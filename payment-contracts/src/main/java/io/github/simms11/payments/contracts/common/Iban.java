package io.github.simms11.payments.contracts.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public record Iban(@JsonValue String value) {

    private static final Pattern FORMAT = Pattern.compile("[A-Z]{2}[0-9]{2}[A-Z0-9]+");

    private static final Map<String, Integer> LENGTH_BY_COUNTRY = Map.of(
            "GB", 22,
            "IE", 22,
            "DE", 22,
            "FR", 27,
            "ES", 24,
            "IT", 27,
            "NL", 18,
            "BE", 16);

    public Iban {
        Objects.requireNonNull(value, "IBAN must not be null");

        value = value.replace(" ", "").toUpperCase(Locale.ROOT);

        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidIbanException("IBAN must be a country code, two check digits and alphanumerics");
        }

        String country = value.substring(0, 2);
        Integer expectedLength = LENGTH_BY_COUNTRY.get(country);
        if (expectedLength == null) {
            throw new InvalidIbanException("IBAN country " + country + " is not supported");
        }
        if (value.length() != expectedLength) {
            throw new InvalidIbanException("IBAN for " + country + " must be " + expectedLength + " characters");
        }

        // Valid check digits are 02-98. 00, 01 and 99 are congruent to 97, 98 and 02 mod 97,
        // so they can pass the checksum and must be rejected explicitly.
        int checkDigits = Integer.parseInt(value.substring(2, 4));
        if (checkDigits < 2 || checkDigits > 98 || mod97(value) != 1) {
            throw new InvalidIbanException("IBAN check digits are invalid");
        }
    }

    @JsonCreator
    public static Iban of(String value) {
        return new Iban(value);
    }

    public String countryCode() {
        return value.substring(0, 2);
    }

    @Override
    public String toString() {
        return value.substring(0, 4) + "*".repeat(value.length() - 8) + value.substring(value.length() - 4);
    }

    private static int mod97(String iban) {
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        int remainder = 0;
        for (char c : rearranged.toCharArray()) {
            int n = Character.getNumericValue(c);
            // Running remainder: appending a digit is ×10, a letter (two digits, 10–35) is ×100.
            remainder = (remainder * (n < 10 ? 10 : 100) + n) % 97;
        }
        return remainder;
    }
}
