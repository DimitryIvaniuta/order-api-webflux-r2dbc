package com.github.dimitryivaniuta.gateway.orderapi.service;

import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {
    Mono<OrderEntity> create(OrderEntity toCreate);

    Mono<OrderEntity> getRequired(long id);

    Flux<OrderEntity> listByCustomerEmail(String email);

    Mono<OrderEntity> updateStatus(long id, String newStatus);
}
