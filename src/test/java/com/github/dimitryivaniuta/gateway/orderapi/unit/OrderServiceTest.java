package com.github.dimitryivaniuta.gateway.orderapi.unit;

import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderEntity;
import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderRepository;
import com.github.dimitryivaniuta.gateway.orderapi.service.OrderEventPublisher;
import com.github.dimitryivaniuta.gateway.orderapi.service.OrderServiceImpl;
import com.github.dimitryivaniuta.gateway.orderapi.web.support.OrderNotFoundException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Test
    void create_sets_server_fields_and_saves_and_publishes_best_effort() {
        var repo = mock(OrderRepository.class);
        var publisher = mock(OrderEventPublisher.class);
        var svc = new OrderServiceImpl(repo, publisher);

        when(repo.save(any())).thenAnswer(inv -> {
            var arg = (OrderEntity) inv.getArgument(0);
            return Mono.just(OrderEntity.builder()
                    .id(1L)
                    .customerEmail(arg.customerEmail())
                    .totalAmount(arg.totalAmount())
                    .status(arg.status())
                    .createdAt(arg.createdAt())
                    .build());
        });

        // publisher fails -> create still succeeds (best-effort)
        when(publisher.publishCreated(any())).thenReturn(Mono.error(new RuntimeException("down")));

        var input = OrderEntity.builder()
                .customerEmail("a@x.com")
                .totalAmount(new BigDecimal("12.34"))
                .status("HACKED")
                .createdAt(Instant.EPOCH)
                .build();

        StepVerifier.create(svc.create(input))
                .expectNextMatches(o -> o.id() == 1L && o.status().equals("NEW") && o.createdAt().isAfter(Instant.EPOCH))
                .verifyComplete();

        verify(repo).save(any());
        verify(publisher).publishCreated(any());
    }

    @Test
    void getRequired_throws_not_found() {
        var repo = mock(OrderRepository.class);
        var publisher = mock(OrderEventPublisher.class);
        var svc = new OrderServiceImpl(repo, publisher);
        when(repo.findById(10L)).thenReturn(Mono.empty());

        StepVerifier.create(svc.getRequired(10L))
                .expectError(OrderNotFoundException.class)
                .verify();
    }

    @Test
    void listByCustomerEmail_delegates() {
        var repo = mock(OrderRepository.class);
        var publisher = mock(OrderEventPublisher.class);
        var svc = new OrderServiceImpl(repo, publisher);

        when(repo.findByCustomerEmail("a@x.com"))
                .thenReturn(Flux.just(OrderEntity.builder().id(1L).customerEmail("a@x.com")
                        .totalAmount(new BigDecimal("1")).status("NEW").createdAt(Instant.now()).build()));

        StepVerifier.create(svc.listByCustomerEmail("a@x.com"))
                .expectNextCount(1)
                .verifyComplete();
    }
}
