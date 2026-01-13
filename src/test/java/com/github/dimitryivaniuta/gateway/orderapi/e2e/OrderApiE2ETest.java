package com.github.dimitryivaniuta.gateway.orderapi.e2e;

import com.github.dimitryivaniuta.gateway.orderapi.testinfra.PostgresTcSupport;
import com.github.dimitryivaniuta.gateway.orderapi.web.dto.CreateOrderRequest;
import com.github.dimitryivaniuta.gateway.orderapi.web.dto.OrderDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class OrderApiE2ETest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = PostgresTcSupport.postgres();

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        PostgresTcSupport.register(r, POSTGRES);
    }

    @Autowired
    WebTestClient client;

    @Test
    void create_then_get_by_id_roundtrip() {
        var create = new CreateOrderRequest("a@x.com", new BigDecimal("12.34"));

        OrderDto created = client.post().uri("/api/orders")
                .bodyValue(create)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();

        client.get().uri("/api/orders/" + created.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(created.id().intValue())
                .jsonPath("$.customerEmail").isEqualTo("a@x.com")
                .jsonPath("$.status").isEqualTo("NEW");
    }

    @Test
    void get_missing_returns_404() {
        client.get().uri("/api/orders/999999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void list_by_email_returns_items() {
        client.post().uri("/api/orders")
                .bodyValue(new CreateOrderRequest("list@x.com", new BigDecimal("1.00")))
                .exchange()
                .expectStatus().isOk();

        client.get().uri(uri -> uri.path("/api/orders").queryParam("email", "list@x.com").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].customerEmail").isEqualTo("list@x.com");
    }
}
