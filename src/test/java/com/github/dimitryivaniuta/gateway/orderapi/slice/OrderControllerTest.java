package com.github.dimitryivaniuta.gateway.orderapi.slice;

import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderEntity;
import com.github.dimitryivaniuta.gateway.orderapi.service.OrderService;
import com.github.dimitryivaniuta.gateway.orderapi.web.OrderController;
import com.github.dimitryivaniuta.gateway.orderapi.web.dto.CreateOrderRequest;
import com.github.dimitryivaniuta.gateway.orderapi.web.dto.UpdateStatusRequest;
import com.github.dimitryivaniuta.gateway.orderapi.web.support.GlobalExceptionHandler;
import com.github.dimitryivaniuta.gateway.orderapi.web.support.OrderNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    WebTestClient client;

    @MockitoBean
    OrderService service;

    @Test
    void create_returns_200_and_body() {
        var saved = OrderEntity.builder()
                .id(10L)
                .customerEmail("a@x.com")
                .totalAmount(new BigDecimal("10.50"))
                .status("NEW")
                .createdAt(Instant.parse("2026-01-07T10:00:00Z"))
                .build();

        when(service.create(any())).thenReturn(Mono.just(saved));

        client.post().uri("/api/orders")
                .bodyValue(new CreateOrderRequest("a@x.com", new BigDecimal("10.50")))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(10)
                .jsonPath("$.customerEmail").isEqualTo("a@x.com")
                .jsonPath("$.status").isEqualTo("NEW");
    }

    @Test
    void create_validation_400() {
        client.post().uri("/api/orders")
                .bodyValue(new CreateOrderRequest("not-an-email", new BigDecimal("-1")))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.details.fieldErrors.customerEmail").exists()
                .jsonPath("$.details.fieldErrors.totalAmount").exists();
    }

    @Test
    void get_not_found_404() {
        when(service.getRequired(99L)).thenReturn(Mono.error(new OrderNotFoundException(99L)));

        client.get().uri("/api/orders/99")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void patch_status_ok() {
        var updated = OrderEntity.builder()
                .id(1L).customerEmail("a@x.com")
                .totalAmount(new BigDecimal("10.50"))
                .status("PAID")
                .createdAt(Instant.parse("2026-01-07T10:00:00Z"))
                .build();

        when(service.updateStatus(1L, "PAID")).thenReturn(Mono.just(updated));

        client.patch().uri("/api/orders/1/status")
                .bodyValue(new UpdateStatusRequest("PAID"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("PAID");
    }
}
