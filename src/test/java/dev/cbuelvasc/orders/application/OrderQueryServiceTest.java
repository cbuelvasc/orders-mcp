package dev.cbuelvasc.orders.application;

import dev.cbuelvasc.orders.domain.exception.OrderNotFoundException;
import dev.cbuelvasc.orders.domain.model.CustomerId;
import dev.cbuelvasc.orders.domain.model.Order;
import dev.cbuelvasc.orders.domain.model.OrderId;
import dev.cbuelvasc.orders.domain.model.OrderStatus;
import dev.cbuelvasc.orders.domain.port.out.OrderRepositoryPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderQueryServiceTest {

    private final RecordingRepository repository = new RecordingRepository();
    private final OrderQueryService service = new OrderQueryService(repository);

    @Test
    void findsAnExistingOrder() {
        var order = service.findById(OrderId.of("ORD-1"));

        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void failsWithAReadableMessageWhenTheOrderDoesNotExist() {
        assertThatThrownBy(() -> service.findById(OrderId.of("ORD-404")))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: ORD-404");
    }

    @Test
    void clampsTheLimitToProtectEveryAdapter() {
        service.listRecent(CustomerId.of("CUS-1"), 500);
        assertThat(repository.lastLimit).isEqualTo(20);

        service.listRecent(CustomerId.of("CUS-1"), -3);
        assertThat(repository.lastLimit).isEqualTo(1);
    }

    @Test
    void rejectsMalformedIdsInTheDomain() {
        assertThatThrownBy(() -> OrderId.of("DROP TABLE orders"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static final class RecordingRepository implements OrderRepositoryPort {

        int lastLimit;

        @Override
        public Optional<Order> findById(OrderId id) {
            return id.value().equals("ORD-1")
                    ? Optional.of(new Order(id, CustomerId.of("CUS-1"), OrderStatus.PAID,
                            BigDecimal.TEN, "USD", Instant.now()))
                    : Optional.empty();
        }

        @Override
        public List<Order> findRecentByCustomer(CustomerId customerId, int limit) {
            lastLimit = limit;
            return List.of();
        }

        @Override
        public Order save(Order order) {
            return order;
        }
    }
}
