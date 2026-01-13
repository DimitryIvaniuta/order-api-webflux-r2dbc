package com.github.dimitryivaniuta.gateway.orderapi.it;

import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderEntity;
import com.github.dimitryivaniuta.gateway.orderapi.domain.OrderRepository;
import com.github.dimitryivaniuta.gateway.orderapi.testinfra.PostgresTcSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;

@Testcontainers
@DataR2dbcTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
class OrderRepositoryTcIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = PostgresTcSupport.postgres();

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        PostgresTcSupport.register(r, POSTGRES);
    }

    @Autowired
    OrderRepository repo;

    @Test
    void save_then_findByCustomerEmail() {
        var order = OrderEntity.builder()
                .customerEmail("a@x.com")
                .totalAmount(new BigDecimal("10.50"))
                .status("NEW")
                .createdAt(Instant.now())
                .build();

        StepVerifier.create(
                        repo.save(order)
                                .flatMapMany(saved -> repo.findByCustomerEmail("a@x.com").take(10))
                )
                .expectNextMatches(it -> it.id() != null && it.customerEmail().equals("a@x.com"))
                .verifyComplete();
    }
}
