package io.github.simms11.payments.contracts.common;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A positive amount in a single currency, held at scale 4 to match NUMERIC(19, 4).
 * The amount is serialised as a JSON string so no consumer parses it as a floating-point number.
 */
public record Money(@JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal amount, Currency currency) {

    private static final int SCALE = 4;
    private static final int MAX_INTEGER_DIGITS = 15;

    public Money {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (amount.stripTrailingZeros().scale() > SCALE) {
            throw new IllegalArgumentException("Amount cannot have more than 4 decimal places");
        }

        amount = amount.setScale(SCALE);

        if (amount.precision() - amount.scale() > MAX_INTEGER_DIGITS) {
            throw new IllegalArgumentException("Amount cannot have more than 15 integer digits");
        }
    }
}
