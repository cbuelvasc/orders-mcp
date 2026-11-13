package dev.cbuelvasc.orders.domain.model;

import dev.cbuelvasc.orders.domain.exception.OrderCannotBeCancelledException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/** Order aggregate. Pure Java: no framework annotations. Immutable: changes return a new Order. */
public record Order(OrderId id,
                    CustomerId customerId,
                    OrderStatus status,
                    BigDecimal total,
                    String currency,
                    Instant updatedAt) {

    public Order {
        Objects.requireNonNull(id);
        Objects.requireNonNull(customerId);
        Objects.requireNonNull(status);
        Objects.requireNonNull(updatedAt);
        if (total == null || total.signum() < 0) {
            throw new IllegalArgumentException("total must be >= 0");
        }
        Objects.requireNonNull(currency);
    }

    /** Business rule: only orders that have not left the warehouse can be cancelled. */
    public Order cancel(Instant now) {
        if (!status.isCancellable()) {
            throw new OrderCannotBeCancelledException(id, status);
        }
        return new Order(id, customerId, OrderStatus.CANCELLED, total, currency, now);
    }
}
