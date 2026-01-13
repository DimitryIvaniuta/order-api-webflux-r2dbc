package com.github.dimitryivaniuta.gateway.orderapi.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotNull @Email String customerEmail,
        @NotNull @Positive BigDecimal totalAmount
) {
}
