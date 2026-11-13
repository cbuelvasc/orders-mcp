package dev.cbuelvasc.orders.infrastructure.adapter.in.mcp;

import dev.cbuelvasc.orders.domain.model.Order;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * What the model sees. The adapter translates the domain into a small,
 * model-friendly shape: only the fields it needs, nothing sensitive.
 */
public record OrderView(String orderId,
                        String status,
                        boolean isFinal,
                        BigDecimal total,
                        String currency,
                        Instant updatedAt) {

    static OrderView from(Order order) {
        return new OrderView(
                order.id().value(),
                order.status().name(),
                order.status().isFinal(),
                order.total(),
                order.currency(),
                order.updatedAt());
    }
}
