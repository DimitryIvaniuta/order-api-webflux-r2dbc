package com.github.dimitryivaniuta.gateway.orderapi.web.support;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public Mono<org.springframework.http.ResponseEntity<ApiError>> notFound(
            OrderNotFoundException ex, ServerHttpRequest req
    ) {
        return Mono.just(build(HttpStatus.NOT_FOUND, ex.getMessage(), req.getPath().value(), null));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<org.springframework.http.ResponseEntity<ApiError>> validation(
            WebExchangeBindException ex, ServerHttpRequest req
    ) {
        Map<String, Object> details = new LinkedHashMap<>();
        Map<String, String> fields = new LinkedHashMap<>();

        for (var err : ex.getAllErrors()) {
            if (err instanceof FieldError fe) {
                fields.put(fe.getField(), fe.getDefaultMessage());
            } else {
                fields.put(err.getObjectName(), err.getDefaultMessage());
            }
        }

        details.put("fieldErrors", fields);
        return Mono.just(build(HttpStatus.BAD_REQUEST, "Validation failed", req.getPath().value(), details));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Mono<ResponseEntity<ApiError>> noResource(
            NoResourceFoundException ex, ServerHttpRequest req
    ) {
        return Mono.just(build(HttpStatus.NOT_FOUND, "Not Found", req.getPath().value(), null));
    }

    @ExceptionHandler(Exception.class)
    public Mono<org.springframework.http.ResponseEntity<ApiError>> generic(
            Exception ex, ServerHttpRequest req
    ) {
        return Mono.just(build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req.getPath().value(),
                Map.of("exception", ex.getClass().getName())));
    }

    private org.springframework.http.ResponseEntity<ApiError> build(
            HttpStatus status, String message, String path, Map<String, Object> details
    ) {
        var body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                details
        );
        return org.springframework.http.ResponseEntity.status(status).body(body);
    }
}
