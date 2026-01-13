package com.github.dimitryivaniuta.gateway.orderapi.web.mapper;

import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderEntity;
import com.github.dimitryivaniuta.gateway.orderapi.web.dto.CreateOrderRequest;
import com.github.dimitryivaniuta.gateway.orderapi.web.dto.OrderDto;

public final class OrderMapper {

    public static OrderEntity toEntity(CreateOrderRequest req) {
        return OrderEntity.builder()
                .customerEmail(req.customerEmail())
                .totalAmount(req.totalAmount())
                .build();
    }

    public static OrderDto toDto(OrderEntity e) {
        return new OrderDto(e.id(), e.customerEmail(), e.totalAmount(), e.status(), e.createdAt());
    }

    private OrderMapper() {
    }
}
