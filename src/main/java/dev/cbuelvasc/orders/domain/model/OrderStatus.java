package dev.cbuelvasc.orders.domain.model;

public enum OrderStatus {
    CREATED, PAID, SHIPPED, DELIVERED, CANCELLED;

    public boolean isFinal() {
        return this == DELIVERED || this == CANCELLED;
    }

    public boolean isCancellable() {
        return this == CREATED || this == PAID;
    }
}
