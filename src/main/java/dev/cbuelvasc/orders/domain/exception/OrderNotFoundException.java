package dev.cbuelvasc.orders.domain.exception;

import dev.cbuelvasc.orders.domain.model.OrderId;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(OrderId id) {
        super("Order not found: " + id.value());
    }
}
