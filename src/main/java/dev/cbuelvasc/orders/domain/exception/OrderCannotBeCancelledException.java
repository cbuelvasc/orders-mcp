package dev.cbuelvasc.orders.domain.exception;

import dev.cbuelvasc.orders.domain.model.OrderId;
import dev.cbuelvasc.orders.domain.model.OrderStatus;

public class OrderCannotBeCancelledException extends RuntimeException {

    public OrderCannotBeCancelledException(OrderId id, OrderStatus status) {
        super("Order %s cannot be cancelled because it is %s".formatted(id.value(), status));
    }
}
