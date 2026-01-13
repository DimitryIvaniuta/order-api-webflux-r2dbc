package com.github.dimitryivaniuta.gateway.orderapi.e2e;

import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderEntity;
import com.github.dimitryivaniuta.gateway.orderapi.service.LoggingOrderEventPublisher;
import com.github.dimitryivaniuta.gateway.orderapi.testinfra.PostgresTcSupport;
import com.github.dimitryivaniuta.gateway.orderapi.web.dto.CreateOrderRequest;
import com.github.dimitryivaniuta.gateway.orderapi.web.dto.OrderDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class OrderApiSpyBeanE2ETest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = PostgresTcSupport.postgres();

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        PostgresTcSupport.register(r, POSTGRES);
    }

    @Autowired
    WebTestClient client;

    @MockitoSpyBean
    LoggingOrderEventPublisher publisher;

    @Test
    void create_order_publishes_created_event() {
        var req = new CreateOrderRequest("spybean@x.com", new BigDecimal("55.55"));

        OrderDto created = client.post().uri("/api/orders")
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();

        var captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(publisher, times(1)).publishCreated(captor.capture());

        var published = captor.getValue();
        assertThat(published.id()).isEqualTo(created.id());
        assertThat(published.customerEmail()).isEqualTo("spybean@x.com");
        assertThat(published.status()).isEqualTo("NEW");
    }
}
