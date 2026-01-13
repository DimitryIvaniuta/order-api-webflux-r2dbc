package com.github.dimitryivaniuta.gateway.orderapi.unit;

import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderEntity;
import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderRepository;
import com.github.dimitryivaniuta.gateway.orderapi.service.OrderEventPayloadFactory;
import com.github.dimitryivaniuta.gateway.orderapi.service.OrderServiceImpl;
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

class OrderServiceWebhookPublisherSpyTest {

    // Mockito can spy a real class (NOT a lambda).
    static final class StubExchangeFunction implements ExchangeFunction {
        @Override
        public Mono<ClientResponse> exchange(ClientRequest request) {
            return Mono.error(new IllegalStateException("Network disabled in unit test"));
        }
    }

    @Test
    void create_saves_order_and_publishes_via_webclient_exchange_spy() {
        OrderRepository repo = mock(OrderRepository.class);

        OrderEntity saved = OrderEntity.builder()
                .id(100L)
                .customerEmail("spy@x.com")
                .totalAmount(new BigDecimal("99.99"))
                .status("NEW")
                .createdAt(Instant.parse("2026-01-07T10:00:00Z"))
                .build();

        when(repo.save(any())).thenReturn(Mono.just(saved));

        ExchangeFunction exchangeSpy = spy(new StubExchangeFunction());

        ClientResponse ok = ClientResponse
                .create(HttpStatus.ACCEPTED)
                .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                .body("")
                .build();

        doReturn(Mono.just(ok)).when(exchangeSpy).exchange(any(ClientRequest.class));

        // payload factory is required now
        OrderEventPayloadFactory payloadFactory = mock(OrderEventPayloadFactory.class);
        when(payloadFactory.createOrderCreated(any(OrderEntity.class)))
                .thenReturn(new WebhookOrderEventPublisher.OrderCreatedEvent(
                        100L, "spy@x.com", "99.99", "NEW", "2026-01-07T00:00:00Z"
                ));

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeSpy).build();
        URI webhook = URI.create("http://example.test/webhook/orders");

        // no need to spy publisher; we verify transport + factory
        WebhookOrderEventPublisher publisher =
                new WebhookOrderEventPublisher(webClient, webhook, payloadFactory);

        OrderServiceImpl service = new OrderServiceImpl(repo, publisher);

        OrderEntity input = OrderEntity.builder()
                .customerEmail("spy@x.com")
                .totalAmount(new BigDecimal("99.99"))
                .status("HACKED")
                .createdAt(Instant.EPOCH)
                .build();

        StepVerifier.create(service.create(input))
                .expectNextMatches(o -> o.id() == 100L && "NEW".equals(o.status()))
                .verifyComplete();

        // verify payload factory used
        verify(payloadFactory, times(1)).createOrderCreated(any(OrderEntity.class));

        // verify HTTP request built
        ArgumentCaptor<ClientRequest> reqCaptor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(exchangeSpy, times(1)).exchange(reqCaptor.capture());

        ClientRequest sent = reqCaptor.getValue();
        assertThat(sent.method().name()).isEqualTo("POST");
        assertThat(sent.url()).isEqualTo(webhook);
        assertThat(sent.headers().getFirst("X-Event-Type")).isEqualTo("OrderCreated");
        assertThat(sent.headers().getFirst(HttpHeaders.CONTENT_TYPE)).contains("application/json");
    }
}
