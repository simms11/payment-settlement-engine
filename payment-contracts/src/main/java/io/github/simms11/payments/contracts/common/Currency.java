package io.github.simms11.payments.contracts.common;

/** Currencies the engine settles in. Matches the CHECK constraint on payments.currency. */
public enum Currency {
    GBP(2),
    EUR(2),
    USD(2);

    private final int minorUnits;

    Currency(int minorUnits) {
        this.minorUnits = minorUnits;
    }

    /** Decimal places of the smallest unit, per ISO 4217 (pence, cents). */
    public int minorUnits() {
        return minorUnits;
    }
}
