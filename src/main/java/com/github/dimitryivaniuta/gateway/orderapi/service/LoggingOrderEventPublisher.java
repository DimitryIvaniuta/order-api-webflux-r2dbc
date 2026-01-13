package com.github.dimitryivaniuta.gateway.orderapi.service;

import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingOrderEventPublisher implements OrderEventPublisher {

    @Override
    public Mono<Void> publishCreated(OrderEntity order) {
        // Real-world: Kafka/outbox/webhook. Here: safe side-effect.
        return Mono.fromRunnable(() -> log.info("OrderCreated published. id={}, email={}, total={}",
                order.id(), order.customerEmail(), order.totalAmount()));
    }
}
