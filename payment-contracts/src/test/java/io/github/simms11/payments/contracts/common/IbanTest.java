package io.github.simms11.payments.contracts.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class IbanTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @ParameterizedTest
    @ValueSource(strings = {
            "GB82WEST12345698765432",
            "IE29AIBK93115212345678",
            "DE89370400440532013000",
            "FR1420041010050500013M02606",
            "ES9121000418450200051332",
            "IT60X0542811101000000123456",
            "NL91ABNA0417164300",
            "BE68539007547034"
    })
    void acceptsRegistryExampleForEachSupportedCountry(String value) {
        Iban iban = Iban.of(value);

        assertThat(iban.value()).isEqualTo(value);
        assertThat(iban.countryCode()).isEqualTo(value.substring(0, 2));
    }

    @Test
    void normalisesSpacesAndCase() {
        Iban iban = Iban.of("gb82 west 1234 5698 7654 32");

        assertThat(iban.value()).isEqualTo("GB82WEST12345698765432");
    }

    @ParameterizedTest
    @ValueSource(strings = {"GB82WEST123456987654!2", "8282WEST12345698765432"})
    void rejectsMalformedInput(String value) {
        assertThatExceptionOfType(InvalidIbanException.class)
                .isThrownBy(() -> Iban.of(value))
                .withMessageContaining("country code, two check digits");
    }

    @Test
    void rejectsUnsupportedCountry() {
        assertThatExceptionOfType(InvalidIbanException.class)
                .isThrownBy(() -> Iban.of("XX82WEST12345698765432"))
                .withMessageContaining("not supported");
    }

    @Test
    void rejectsWrongLengthForCountry() {
        assertThatExceptionOfType(InvalidIbanException.class)
                .isThrownBy(() -> Iban.of("GB82WEST1234569876543"))
                .withMessageContaining("must be 22 characters");
    }

    @Test
    void rejectsInvalidCheckDigits() {
        assertThatExceptionOfType(InvalidIbanException.class)
                .isThrownBy(() -> Iban.of("GB82WEST12345698765431"))
                .withMessageContaining("check digits");
    }

    // GB97WEST12345698760021 is valid; 00 is congruent to 97 mod 97, so it passes the checksum alone
    @Test
    void rejectsOutOfRangeCheckDigitsThatSatisfyTheChecksum() {
        assertThatExceptionOfType(InvalidIbanException.class)
                .isThrownBy(() -> Iban.of("GB00WEST12345698760021"))
                .withMessageContaining("check digits");
    }

    @Test
    void masksValueInToString() {
        Iban iban = Iban.of("GB82WEST12345698765432");

        assertThat(iban.toString()).isEqualTo("GB82**************5432");
    }

    @Test
    void serialisesAsPlainStringAndRoundTrips() {
        Iban original = Iban.of("GB82WEST12345698765432");

        String json = jsonMapper.writeValueAsString(original);

        assertThat(json).isEqualTo("\"GB82WEST12345698765432\"");
        assertThat(jsonMapper.readValue(json, Iban.class)).isEqualTo(original);
    }
}
