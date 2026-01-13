package com.github.dimitryivaniuta.gateway.orderapi.unit;

import com.github.dimitryivaniuta.gateway.orderapi.web.dto.CreateOrderRequest;
import com.github.dimitryivaniuta.gateway.orderapi.web.mapper.OrderMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    @Test
    void toEntity_maps_fields() {
        var req = new CreateOrderRequest("a@x.com", new BigDecimal("10.00"));
        var e = OrderMapper.toEntity(req);

        assertThat(e.id()).isNull();
        assertThat(e.customerEmail()).isEqualTo("a@x.com");
        assertThat(e.totalAmount()).isEqualByComparingTo("10.00");
    }
}
