package com.github.dimitryivaniuta.gateway.orderapi.service;

import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderEntity;
import reactor.core.publisher.Mono;

public interface OrderEventPublisher {
    Mono<Void> publishCreated(OrderEntity order);
}
