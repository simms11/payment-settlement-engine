package io.github.simms11.payments.contracts.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class MoneyTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void amountsDifferingOnlyInTrailingZerosAreEqual() {
        Money a = new Money(new BigDecimal("100.5"), Currency.GBP);
        Money b = new Money(new BigDecimal("100.50"), Currency.GBP);

        assertThat(a).isEqualTo(b);
    }

    @Test
    void acceptsTrailingZerosBeyondMinorUnits() {
        Money money = new Money(new BigDecimal("1.230000"), Currency.EUR);

        assertThat(money.amount()).isEqualTo(new BigDecimal("1.23"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.01"})
    void rejectsZeroAndNegativeAmounts(String amount) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Money(new BigDecimal(amount), Currency.GBP))
                .withMessageContaining("greater than zero");
    }

    @ParameterizedTest
    @EnumSource(Currency.class)
    void rejectsMoreDecimalPlacesThanTheCurrencyAllows(Currency currency) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Money(new BigDecimal("1.234"), currency))
                .withMessageContaining("more than 2 decimal places");
    }

    @Test
    void rejectsAmountTooLargeForTheColumn() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Money(new BigDecimal("1000000000000000"), Currency.GBP))
                .withMessageContaining("15 integer digits");
    }

    @Test
    void serialisesAmountAsString() {
        String json = jsonMapper.writeValueAsString(new Money(new BigDecimal("100"), Currency.GBP));

        assertThat(json).isEqualTo("""
                {"amount":"100.00","currency":"GBP"}""");
    }

    @Test
    void roundTripsThroughJson() {
        Money original = new Money(new BigDecimal("42.5"), Currency.EUR);

        Money parsed = jsonMapper.readValue(jsonMapper.writeValueAsString(original), Money.class);

        assertThat(parsed).isEqualTo(original);
    }
}
