package com.github.dimitryivaniuta.gateway.orderapi.testinfra;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

public final class PostgresTcSupport {

    private PostgresTcSupport() {
    }

    public static PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
    }

    public static void register(DynamicPropertyRegistry r, PostgreSQLContainer<?> pg) {
        // R2DBC (app)
        r.add("spring.r2dbc.url", () -> "r2dbc:postgresql://%s:%d/%s"
                .formatted(pg.getHost(), pg.getMappedPort(5432), pg.getDatabaseName()));
        r.add("spring.r2dbc.username", pg::getUsername);
        r.add("spring.r2dbc.password", pg::getPassword);

        // Flyway (JDBC migrations)
        r.add("spring.flyway.url", pg::getJdbcUrl);
        r.add("spring.flyway.user", pg::getUsername);
        r.add("spring.flyway.password", pg::getPassword);

        r.add("spring.test.database.replace", () -> "NONE");
    }
}
