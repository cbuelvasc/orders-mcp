package dev.cbuelvasc.orders.application;

import dev.cbuelvasc.orders.domain.exception.OrderCannotBeCancelledException;
import dev.cbuelvasc.orders.domain.exception.OrderNotFoundException;
import dev.cbuelvasc.orders.domain.model.CustomerId;
import dev.cbuelvasc.orders.domain.model.Order;
import dev.cbuelvasc.orders.domain.model.OrderId;
import dev.cbuelvasc.orders.domain.model.OrderStatus;
import dev.cbuelvasc.orders.infrastructure.adapter.out.memory.InMemoryOrderRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancelOrderServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-13T15:00:00Z");

    private final InMemoryOrderRepository repository = new InMemoryOrderRepository();
    private final CancelOrderService service =
            new CancelOrderService(repository, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void cancelsAPaidOrder() {
        repository.save(order("ORD-1", OrderStatus.PAID));

        var cancelled = service.cancel(OrderId.of("ORD-1"));

        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelled.updatedAt()).isEqualTo(NOW);
        assertThat(repository.findById(OrderId.of("ORD-1"))).contains(cancelled);
    }

    @Test
    void refusesToCancelAShippedOrder() {
        repository.save(order("ORD-2", OrderStatus.SHIPPED));

        assertThatThrownBy(() -> service.cancel(OrderId.of("ORD-2")))
                .isInstanceOf(OrderCannotBeCancelledException.class)
                .hasMessage("Order ORD-2 cannot be cancelled because it is SHIPPED");
    }

    @Test
    void failsForUnknownOrders() {
        assertThatThrownBy(() -> service.cancel(OrderId.of("ORD-404")))
                .isInstanceOf(OrderNotFoundException.class);
    }

    private static Order order(String id, OrderStatus status) {
        return new Order(OrderId.of(id), CustomerId.of("CUS-1"), status, BigDecimal.TEN, "USD",
                NOW.minusSeconds(3600));
    }
}
