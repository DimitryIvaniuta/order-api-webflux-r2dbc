package com.github.dimitryivaniuta.gateway.orderapi.unit;

import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderEntity;
import com.github.dimitryivaniuta.gateway.orderapi.service.DefaultOrderEventPayloadFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultOrderEventPayloadFactoryTest {

    @Test
    void creates_payload_from_order() {
        var factory = new DefaultOrderEventPayloadFactory();

        var order = OrderEntity.builder()
                .id(7L)
                .customerEmail("fixed@x.com")
                .totalAmount(new BigDecimal("1.23"))
                .status("NEW")
                .createdAt(Instant.parse("2026-01-07T00:00:00Z"))
                .build();

        var payload = factory.createOrderCreated(order);

        assertThat(payload.id()).isEqualTo(7L);
        assertThat(payload.customerEmail()).isEqualTo("fixed@x.com");
        assertThat(payload.totalAmount()).isEqualTo("1.23");
        assertThat(payload.status()).isEqualTo("NEW");
        assertThat(payload.publishedAt()).isNotBlank(); // time is generated "now"
    }
}
