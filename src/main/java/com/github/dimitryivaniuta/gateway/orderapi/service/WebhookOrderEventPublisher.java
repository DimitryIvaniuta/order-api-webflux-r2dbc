package com.github.dimitryivaniuta.gateway.orderapi.service;

import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * WebClient-based publisher example.
 * Payload creation is delegated to OrderEventPayloadFactory (testable without spying internals).
 */
public final class WebhookOrderEventPublisher implements OrderEventPublisher {

    private final WebClient webClient;
    private final URI webhookUri;
    private final OrderEventPayloadFactory payloadFactory;

    public WebhookOrderEventPublisher(WebClient webClient, URI webhookUri, OrderEventPayloadFactory payloadFactory) {
        this.webClient = webClient;
        this.webhookUri = webhookUri;
        this.payloadFactory = payloadFactory;
    }

    @Override
    public Mono<Void> publishCreated(OrderEntity order) {
        OrderCreatedEvent payload = payloadFactory.createOrderCreated(order);

        return webClient.post()
                .uri(webhookUri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("X-Event-Type", "OrderCreated")
                .bodyValue(payload)
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is2xxSuccessful()) {
                        return resp.releaseBody();
                    }
                    HttpStatusCode code = resp.statusCode();
                    return resp.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new WebhookPublishException(code.value(), body)));
                });
    }

    public record OrderCreatedEvent(Long id, String customerEmail, String totalAmount, String status, String publishedAt) {}

    public static final class WebhookPublishException extends RuntimeException {
        public WebhookPublishException(int status, String body) {
            super("Webhook publish failed. status=" + status + ", body=" + body);
        }
    }
}
