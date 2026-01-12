package com.github.dimitryivaniuta.gateway.orderapi.domain;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("orders")
@Builder
public record OrderEntity(
        @Id Long id,
        String customerEmail,
        BigDecimal totalAmount,
        String status,
        Instant createdAt
) {
}
