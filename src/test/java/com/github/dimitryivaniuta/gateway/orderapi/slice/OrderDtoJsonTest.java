package com.github.dimitryivaniuta.gateway.orderapi.slice;

import com.github.dimitryivaniuta.gateway.orderapi.web.dto.OrderDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class OrderDtoJsonTest {

    @Autowired
    JacksonTester<OrderDto> json;

    @Test
    void serialize_contains_fields() throws Exception {
        var dto = new OrderDto(1L, "a@x.com", new BigDecimal("10.50"), "NEW", Instant.parse("2026-01-07T10:00:00Z"));
        var content = json.write(dto);

        assertThat(content).hasJsonPathValue("$.id");
        assertThat(content).extractingJsonPathStringValue("$.customerEmail").isEqualTo("a@x.com");
        assertThat(content).extractingJsonPathStringValue("$.status").isEqualTo("NEW");
    }
}
