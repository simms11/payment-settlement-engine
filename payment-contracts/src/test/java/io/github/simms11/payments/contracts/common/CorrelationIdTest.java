package io.github.simms11.payments.contracts.common;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void serialisesAsBareUuidString() {
        UUID uuid = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");

        String json = jsonMapper.writeValueAsString(CorrelationId.of(uuid));

        assertThat(json).isEqualTo("\"7c9e6679-7425-40de-944b-e07fc1f90ae7\"");
    }

    @Test
    void roundTripsThroughJson() {
        CorrelationId original = CorrelationId.generate();

        CorrelationId parsed = jsonMapper.readValue(jsonMapper.writeValueAsString(original), CorrelationId.class);

        assertThat(parsed).isEqualTo(original);
    }
}
