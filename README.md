# order-api-webflux-r2dbc

Reactive **Order API** microservice built with **Java 21** and **Spring Boot 3.5.x (WebFlux)** using **PostgreSQL** via **R2DBC** and DB migrations via **Flyway (JDBC)**.  
Includes a full, production-style test suite: **unit**, **slice**, **repository integration (Testcontainers)**, and **E2E** (real HTTP server + real DB).

Package root: `com.github.dimitryivaniuta.gateway.orderapi`

---

## Tech stack

- Java 21
- Spring Boot: WebFlux, Validation, Data R2DBC
- PostgreSQL
  - runtime (reactive): `org.postgresql:r2dbc-postgresql`
  - Flyway migrations (JDBC): `org.postgresql:postgresql`
- Flyway
- Lombok
- Tests: JUnit 5, Reactor Test, Mockito, Testcontainers (PostgreSQL)

---

## Source structure

### Main (`src/main/java/com/github/dimitryivaniuta/gateway/orderapi`)
- `config/ApiConfig` – enables R2DBC repositories
- `domain/`
  - `OrderEntity` – R2DBC entity (record)
  - `OrderRepository` – `ReactiveCrudRepository`
  - `OrderStatus` – app-level statuses
- `service/`
  - `OrderService`, `OrderServiceImpl` – business logic
  - `OrderEventPublisher` – side-effect boundary
  - `LoggingOrderEventPublisher` – default publisher bean (logs)
  - Webhook example:
    - `WebhookOrderEventPublisher` – WebClient-based publisher (HTTP)
    - `OrderEventPayloadFactory`, `DefaultOrderEventPayloadFactory` – payload creation extracted for testability
- `web/`
  - `OrderController`
  - `dto/` – `CreateOrderRequest`, `UpdateStatusRequest`, `OrderDto`
  - `mapper/OrderMapper`
  - `support/` – `GlobalExceptionHandler`, `ApiError`, `OrderNotFoundException`

### Resources
- `src/main/resources/application.yml`
- `src/main/resources/db/migration/V1__create_orders.sql`

### Tests (`src/test/java/...`)
- `unit/` – fast, no Spring
- `slice/` – `@WebFluxTest`, `@JsonTest`
- `it/` – repository IT with Testcontainers (`@DataR2dbcTest`)
- `e2e/` – full E2E (`@SpringBootTest(RANDOM_PORT)`)
- `testinfra/` – `PostgresTcSupport`

---

## API

Base: `/api/orders`

### Create
`POST /api/orders`

```json
{ "customerEmail": "a@x.com", "totalAmount": 12.34 }
```

### Get by id
`GET /api/orders/{id}`

### List by email
`GET /api/orders?email=a@x.com`

### Update status
`PATCH /api/orders/{id}/status`

```json
{ "status": "PAID" }
```

---

## Configuration

### `.env` (optional)
```env
ORDER_R2DBC_URL=r2dbc:postgresql://localhost:5432/orderdb
ORDER_JDBC_URL=jdbc:postgresql://localhost:5432/orderdb
ORDER_DB_USER=order
ORDER_DB_PASS=order
SERVER_PORT=8080
```

- Runtime uses **R2DBC**.
- Flyway uses **JDBC** (same DB).

---

## Run locally

Start Postgres:
```bash
docker compose up -d
```

Run app:
```bash
./gradlew bootRun
```

---

## Database migrations

Migrations:
- `src/main/resources/db/migration`

Current:
- `V1__create_orders.sql` creates `orders` + index on `customer_email`

---

## Tests

Run all:
```bash
./gradlew clean test
```

### Unit (`unit/`)
- `OrderServiceTest` – service logic with mocked repo/publisher (StepVerifier)
- `OrderMapperTest` – mapping
- `DefaultOrderEventPayloadFactoryTest` – payload mapping
- `WebhookOrderEventPublisherWithFactoryTest` – publisher uses factory + HTTP is stubbed via `ExchangeFunction` (no network)
- `OrderServiceWebhookPublisherSpyTest` – service flow with mocked repo and stubbed WebClient transport

### Slice (`slice/`)
- `OrderControllerTest` – `@WebFluxTest` controller slice
- `OrderDtoJsonTest` – `@JsonTest` JSON checks

> Spring Boot 3.4+ deprecates `@MockBean`; prefer `@MockitoBean` for new code.

### Repository IT (`it/`)
- `OrderRepositoryTcIT` – `@DataR2dbcTest` + Testcontainers Postgres + Flyway migrations

### E2E (`e2e/`)
- `OrderApiE2ETest` – real server + real DB
- `OrderApiSpyBeanE2ETest` – verifies side-effect bean call via `@SpyBean`

---

## Run single test (Gradle)

Class:
```bash
./gradlew test --tests "com.github.dimitryivaniuta.gateway.orderapi.unit.WebhookOrderEventPublisherWithFactoryTest"
```

Method:
```bash
./gradlew test --tests "com.github.dimitryivaniuta.gateway.orderapi.unit.WebhookOrderEventPublisherWithFactoryTest.publishCreated_uses_factory_and_posts_to_webhook"
```

If Gradle says **“No matching tests found”**, usually the test didn’t compile (e.g., leftover `...`) or package/path mismatch.

---

## Mockito “self-attaching” warning

Newer JDKs warn about Mockito/ByteBuddy dynamic agent attach.  
Best-practice: attach ByteBuddy agent explicitly for tests using a dedicated configuration + `jvmArgumentProviders` in Gradle (configuration-cache friendly).

---

## Troubleshooting

- R2DBC driver dependency:
  - ✅ `org.postgresql:r2dbc-postgresql`
  - ❌ `io.r2dbc:r2dbc-postgresql`

---

## 📜 License

MIT

---

## Contact

**Dimitry Ivaniuta** — [dzmitry.ivaniuta.services@gmail.com](mailto:dzmitry.ivaniuta.services@gmail.com) — [GitHub](https://github.com/DimitryIvaniuta)

