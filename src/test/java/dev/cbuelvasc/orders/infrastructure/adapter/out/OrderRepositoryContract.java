package dev.cbuelvasc.orders.infrastructure.adapter.out;

import dev.cbuelvasc.orders.domain.model.CustomerId;
import dev.cbuelvasc.orders.domain.model.Order;
import dev.cbuelvasc.orders.domain.model.OrderId;
import dev.cbuelvasc.orders.domain.model.OrderStatus;
import dev.cbuelvasc.orders.domain.port.out.OrderRepositoryPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract for the outbound port. EVERY adapter (in-memory, PostgreSQL, ...) must pass it.
 * This is what makes swapping adapters safe.
 */
public abstract class OrderRepositoryContract {

    protected abstract OrderRepositoryPort repository();

    private static final Instant NOW = Instant.parse("2026-11-13T12:00:00Z");

    @Test
    void savesAndFindsAnOrder() {
        var order = order("ORD-9001", "CUS-99", OrderStatus.PAID, NOW);

        repository().save(order);

        assertThat(repository().findById(order.id())).contains(order);
    }

    @Test
    void returnsEmptyForUnknownOrders() {
        assertThat(repository().findById(OrderId.of("ORD-9999999"))).isEmpty();
    }

    @Test
    void listsTheMostRecentOrdersOfOneCustomerNewestFirst() {
        repository().save(order("ORD-9101", "CUS-98", OrderStatus.PAID, NOW.minus(3, ChronoUnit.HOURS)));
        repository().save(order("ORD-9102", "CUS-98", OrderStatus.PAID, NOW.minus(1, ChronoUnit.HOURS)));
        repository().save(order("ORD-9103", "CUS-98", OrderStatus.PAID, NOW.minus(2, ChronoUnit.HOURS)));
        repository().save(order("ORD-9104", "CUS-97", OrderStatus.PAID, NOW));

        var recent = repository().findRecentByCustomer(CustomerId.of("CUS-98"), 2);

        assertThat(recent).extracting(o -> o.id().value()).containsExactly("ORD-9102", "ORD-9103");
    }

    @Test
    void saveUpdatesAnExistingOrder() {
        var order = order("ORD-9201", "CUS-96", OrderStatus.PAID, NOW);
        repository().save(order);

        repository().save(order.cancel(NOW.plusSeconds(60)));

        assertThat(repository().findById(order.id()))
                .hasValueSatisfying(saved -> assertThat(saved.status()).isEqualTo(OrderStatus.CANCELLED));
    }

    private static Order order(String id, String customer, OrderStatus status, Instant updatedAt) {
        return new Order(OrderId.of(id), CustomerId.of(customer), status,
                new BigDecimal("42.50"), "USD", updatedAt);
    }
}
