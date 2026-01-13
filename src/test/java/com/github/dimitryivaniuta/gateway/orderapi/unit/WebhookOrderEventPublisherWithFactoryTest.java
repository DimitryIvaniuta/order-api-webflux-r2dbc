package com.github.dimitryivaniuta.gateway.orderapi.unit;

import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderEntity;
import com.github.dimitryivaniuta.gateway.orderapi.service.OrderEventPayloadFactory;
import com.github.dimitryivaniuta.gateway.orderapi.service.WebhookOrderEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WebhookOrderEventPublisherWithFactoryTest {

    static final class StubExchangeFunction implements ExchangeFunction {
        @Override public Mono<ClientResponse> exchange(ClientRequest request) {
            return Mono.error(new IllegalStateException("Network disabled"));
        }
    }

    @Test
    public void publishCreated_uses_factory_and_posts_to_webhook() {
        ExchangeFunction exchangeSpy = spy(new StubExchangeFunction());
        doReturn(Mono.just(ClientResponse.create(HttpStatus.OK).body("").build()))
                .when(exchangeSpy).exchange(any(ClientRequest.class));

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeSpy).build();

        OrderEventPayloadFactory factory = mock(OrderEventPayloadFactory.class);
        when(factory.createOrderCreated(any(OrderEntity.class)))
                .thenReturn(new WebhookOrderEventPublisher.OrderCreatedEvent(
                        7L, "fixed@x.com", "1.23", "NEW", "2026-01-07T00:00:00Z"
                ));

        URI webhook = URI.create("http://example.test/webhook");
        var publisher = new WebhookOrderEventPublisher(webClient, webhook, factory);

        var order = OrderEntity.builder()
                .id(7L)
                .customerEmail("fixed@x.com")
                .totalAmount(new BigDecimal("1.23"))
                .status("NEW")
                .createdAt(Instant.now())
                .build();

        StepVerifier.create(publisher.publishCreated(order))
                .verifyComplete();

        verify(factory, times(1)).createOrderCreated(order);

        ArgumentCaptor<ClientRequest> reqCaptor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(exchangeSpy, times(1)).exchange(reqCaptor.capture());

        ClientRequest sent = reqCaptor.getValue();
        assertThat(sent.method().name()).isEqualTo("POST");
        assertThat(sent.url()).isEqualTo(webhook);
        assertThat(sent.headers().getFirst("X-Event-Type")).isEqualTo("OrderCreated");
        assertThat(sent.headers().getFirst(HttpHeaders.CONTENT_TYPE)).contains("application/json");
    }
}
