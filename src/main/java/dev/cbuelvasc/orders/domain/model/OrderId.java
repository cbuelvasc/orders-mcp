package dev.cbuelvasc.orders.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/** Value object: an order id always has the format ORD-&lt;digits&gt;. */
public record OrderId(String value) {

    private static final Pattern FORMAT = Pattern.compile("ORD-\\d{1,10}");

    public OrderId {
        Objects.requireNonNull(value, "order id is required");
        value = value.strip().toUpperCase();
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid order id '%s'. Expected format ORD-<digits>".formatted(value));
        }
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }
}
