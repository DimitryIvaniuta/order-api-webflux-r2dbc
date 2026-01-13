package com.github.dimitryivaniuta.gateway.orderapi.service;

import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DefaultOrderEventPayloadFactory implements OrderEventPayloadFactory {

    @Override
    public WebhookOrderEventPublisher.OrderCreatedEvent createOrderCreated(OrderEntity o) {
        return new WebhookOrderEventPublisher.OrderCreatedEvent(
                o.id(),
                o.customerEmail(),
                o.totalAmount() == null ? null : o.totalAmount().toPlainString(),
                o.status(),
                Instant.now().toString()
        );
    }
}
