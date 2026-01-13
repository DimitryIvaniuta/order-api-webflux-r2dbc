package com.github.dimitryivaniuta.gateway.orderapi.service;

import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderEntity;

public interface OrderEventPayloadFactory {
    WebhookOrderEventPublisher.OrderCreatedEvent createOrderCreated(OrderEntity order);
}
