package com.github.dimitryivaniuta.gateway.orderapi.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderDto(
        Long id,
        String customerEmail,
        BigDecimal totalAmount,
        String status,
        Instant createdAt
) {
}
