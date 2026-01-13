package com.github.dimitryivaniuta.gateway.orderapi.web.support;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(long id) {
        super("Order not found: " + id);
    }
}
