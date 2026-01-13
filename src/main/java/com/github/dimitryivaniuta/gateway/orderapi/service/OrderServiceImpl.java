package com.github.dimitryivaniuta.gateway.orderapi.service;

import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderEntity;
import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderRepository;
import com.github.dimitryivaniuta.gateway.orderapi.web.support.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repo;
    private final OrderEventPublisher publisher;

    @Override
    public Mono<OrderEntity> create(OrderEntity toCreate) {
        var normalized = OrderEntity.builder()
                .id(null)
                .customerEmail(toCreate.customerEmail())
                .totalAmount(toCreate.totalAmount())
                .status("NEW")
                .createdAt(Instant.now())
                .build();

        return repo.save(normalized)
                .flatMap(saved ->
                        publisher.publishCreated(saved)
                                // Best-effort: do not fail order creation due to event publish issue.
                                .onErrorResume(ex -> {
                                    log.warn("Order created but event publish failed. orderId={}, cause={}",
                                            saved.id(), ex.toString());
                                    return Mono.empty();
                                })
                                .thenReturn(saved)
                );
    }

    @Override
    public Mono<OrderEntity> getRequired(long id) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(id)));
    }

    @Override
    public Flux<OrderEntity> listByCustomerEmail(String email) {
        return repo.findByCustomerEmail(email);
    }

    @Override
    public Mono<OrderEntity> updateStatus(long id, String newStatus) {
        return getRequired(id)
                .flatMap(existing -> repo.save(OrderEntity.builder()
                        .id(existing.id())
                        .customerEmail(existing.customerEmail())
                        .totalAmount(existing.totalAmount())
                        .status(newStatus)
                        .createdAt(existing.createdAt())
                        .build()));
    }
}
