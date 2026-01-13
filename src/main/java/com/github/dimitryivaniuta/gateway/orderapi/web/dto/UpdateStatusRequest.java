package com.github.dimitryivaniuta.gateway.orderapi.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateStatusRequest(@NotBlank String status) {
}
