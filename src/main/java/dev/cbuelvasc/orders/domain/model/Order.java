package dev.cbuelvasc.orders.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/** Order aggregate (read side). Pure Java: no framework annotations. */
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
}
