package io.github.simms11.payments.contracts.common;

public class InvalidIbanException extends IllegalArgumentException {

    public InvalidIbanException(String message) {
        super(message);
    }
}
