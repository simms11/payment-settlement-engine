package io.github.simms11.payments.contracts.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.UUID;

public record CorrelationId(@JsonValue UUID value) {

    public CorrelationId {
        Objects.requireNonNull(value, "Correlation ID must not be null");
    }

    @JsonCreator
    public static CorrelationId of(UUID value) {
        return new CorrelationId(value);
    }

    public static CorrelationId generate() {
        return new CorrelationId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
