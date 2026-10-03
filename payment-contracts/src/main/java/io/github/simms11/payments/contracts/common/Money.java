package io.github.simms11.payments.contracts.common;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A positive payment amount, held at its currency's minor units (e.g. 100.50 GBP).
 * The amount is serialised as a JSON string so no consumer parses it as a floating-point number.
 */
public record Money(@JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal amount, Currency currency) {

    // NUMERIC(19, 4) leaves 15 digits before the decimal point
    private static final int MAX_INTEGER_DIGITS = 15;

    public Money {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (amount.stripTrailingZeros().scale() > currency.minorUnits()) {
            throw new IllegalArgumentException(
                    currency + " amounts cannot have more than " + currency.minorUnits() + " decimal places");
        }

        amount = amount.setScale(currency.minorUnits());

        if (amount.precision() - amount.scale() > MAX_INTEGER_DIGITS) {
            throw new IllegalArgumentException("Amount cannot have more than 15 integer digits");
        }
    }
}
