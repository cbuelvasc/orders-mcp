package dev.cbuelvasc.orders.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/** Value object: a customer id always has the format CUS-&lt;digits&gt;. */
public record CustomerId(String value) {

    private static final Pattern FORMAT = Pattern.compile("CUS-\\d{1,10}");

    public CustomerId {
        Objects.requireNonNull(value, "customer id is required");
        value = value.strip().toUpperCase();
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid customer id '%s'. Expected format CUS-<digits>".formatted(value));
        }
    }

    public static CustomerId of(String value) {
        return new CustomerId(value);
    }
}
