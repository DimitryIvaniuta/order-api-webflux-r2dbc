package com.github.dimitryivaniuta.gateway.orderapi.web;

import com.github.dimitryivaniuta.gateway.orderapi.service.OrderService;
import com.github.dimitryivaniuta.gateway.orderapi.web.dto.CreateOrderRequest;
import com.github.dimitryivaniuta.gateway.orderapi.web.dto.OrderDto;
import com.github.dimitryivaniuta.gateway.orderapi.web.dto.UpdateStatusRequest;
import com.github.dimitryivaniuta.gateway.orderapi.web.mapper.OrderMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    public Mono<ResponseEntity<OrderDto>> create(@Valid @RequestBody CreateOrderRequest req) {
        return service.create(OrderMapper.toEntity(req))
                .map(OrderMapper::toDto)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<OrderDto> get(@PathVariable long id) {
        return service.getRequired(id).map(OrderMapper::toDto);
    }

    @GetMapping
    public Flux<OrderDto> list(@RequestParam String email) {
        return service.listByCustomerEmail(email).map(OrderMapper::toDto);
    }

    @PatchMapping("/{id}/status")
    public Mono<OrderDto> updateStatus(@PathVariable long id, @Valid @RequestBody UpdateStatusRequest req) {
        return service.updateStatus(id, req.status()).map(OrderMapper::toDto);
    }
}
