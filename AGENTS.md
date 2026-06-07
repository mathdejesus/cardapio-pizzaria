# Cardapio — Spring Boot REST API

Pizzaria menu API with JWT auth, soft delete, Redis cache, rate limiting, HATEOAS.

> **Documentação legacy** (auditoria de issues, relatório de sessão) está em [`docs/legacy/`](docs/legacy/README.md). A fonte canônica do estado atual do projeto é este `AGENTS.md` e o `README.md` principal.

## Quick start (dev, no infra needed)

```bash
export JWT_SECRET="minha-chave-super-segura-com-pelo-menos-32-caracteres!"
./mvnw spring-boot:run
```

H2 console at `/h2-console`, cache Caffeine, no Redis needed. Seed data auto-loaded by `DataInitializer` (ApplicationRunner, skips if data exists).

## Profiles

| Profile | DB | Cache | Rate limiter | Flyway | `app.redis.enabled` |
|---------|----|-------|-------------|--------|---------------------|
| default | H2 in-mem | Caffeine | InMemory | disabled | `false` |
| postgres | PostgreSQL | Redis | Redis | enabled (`ddl-auto=validate`) | `true` |

Activate: `./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres`

All Redis/InMemory switching is driven by `app.redis.enabled` via `@ConditionalOnProperty`. Property in `application.properties` (default=`false`), overridden in `application-postgres.properties` (`true`).

## Tests (76 total — no infra required)

```bash
./mvnw test                                         # all 75
./mvnw test -Dtest="*ServiceTest"                   # service unit tests (33)
./mvnw test -Dtest="*ControllerTest"                # controller tests (21)
./mvnw test -Dtest="*StoreTest,*RateLimiterTest"    # infra unit tests (16)
./mvnw test -Dtest="JwtServiceTest"                 # JWT unit tests (10)
./mvnw test -Dtest="UserServiceTest"                # user service tests (3)
./mvnw test -Dtest="PedidoServiceTest"              # pedido service tests (10)
./mvnw test -Dtest="PedidoControllerTest"           # pedido controller tests (7)
./mvnw test -Dtest="UploadControllerTest"           # upload controller tests (6)
./mvnw test -Dtest="PizzaServiceTest#testName"      # single method
```

H2 in-memory, Redis excluded. `TestConfig` mocks 5 beans: `LoginRateLimiter`, `TokenStore`, `CardapioMetrics`, `JwtService`, `UserDetailsService`. Tests use `application-test.properties` (`spring.cache.type=none`). Controller tests use `@SpringBootTest` + `@AutoConfigureMockMvc` + `@WithMockUser` for role-based auth.

## Architecture quirks (agent will miss)

- **Soft delete**: `@SQLRestriction("deleted = false")` on `Pizza` entity — all JPA derived queries auto-filter deleted rows. If you add `@Query` methods in the future, you must add `WHERE deleted = false` manually.
- **Dual `List` collections on Pizza**: `tamanhos` + `ingredientes` → `@Fetch(FetchMode.SUBSELECT)` required to avoid Hibernate `MultipleBagFetchException`. Do NOT use `@EntityGraph` or JOIN FETCH here.
- **Dirty checking**: `PizzaService.update`/`updateDisponibilidade` do NOT call `save()` — JPA dirty checking flushes managed entities automatically within `@Transactional`.
- **MapStruct**: `PizzaMapper` generates at compile time via annotation processor. After changing mapper, run `./mvnw clean compile` before tests.
- **Cache**: `@Cacheable("cardapio")` on `CardapioService.getCardapio()`; all write methods in `PizzaService` do `@CacheEvict(value = "cardapio", allEntries = true)`. Profile `default` uses Caffeine, `postgres` uses Redis.
- **Pedido ownership**: `PedidoService.buscarPedido(id, email)` valida que o pedido pertence ao email. Use `buscarPedidoAdmin(id)` (sem parâmetro email) para endpoints admin que não precisam validar ownership.
- **UploadController**: POST requer autenticação; GET é público. Defesa contra path traversal (`..`, `/`, `\\`) é aplicada antes do `Files.probeContentType`.
- **SecurityConfig matchers order**: matchers mais específicos devem vir ANTES dos genéricos. Exemplo correto: `/api/pedidos/admin/**` com `hasRole("ADMIN")` antes de `/api/pedidos/**` com `authenticated()`. Inversão causa bypass de role.
- **Dual implementations via ConditionalOnProperty**: `InMemoryTokenStore`/`RedisTokenStore` and `InMemoryLoginRateLimiter`/`RedisLoginRateLimiter` swap based on `app.redis.enabled`. Both pairs implement the same interface (`TokenStore`, `LoginRateLimiter`).
- **Actuator security**: Separate `SecurityFilterChain` (`@Order(0)`) in `ActuatorSecurityConfig` — permits `health`, `info`, `metrics`, `prometheus`; denies all other actuator endpoints.
- **JWT_SECRET validated at startup**: `JwtService.validateSecret()` (`@PostConstruct`) throws `IllegalStateException` if secret < 32 chars.
- **UserService**: Separate from `AuthService` — `UserService.getProfile()` returns `UserProfileDTO` for `GET /api/auth/me`. `AuthService` handles login/refresh/logout.

## Key env vars

| Var | Required | Notes |
|-----|----------|-------|
| `JWT_SECRET` | **Yes** | 32+ chars; validated at startup |
| `DATABASE_URL` | postgres only | Default: `jdbc:postgresql://localhost:5432/pizzaria` |
| `DATABASE_USER` | postgres only | Default: `postgres` |
| `DATABASE_PASSWORD` | postgres only | Default: `postgres` |
| `SPRING_DATA_REDIS_HOST` | postgres only | Default: `localhost` |
| `SPRING_DATA_REDIS_PORT` | postgres only | Default: `6379` |

## Seed data

| Role | Email | Password |
|------|-------|----------|
| ADMIN | `admin@pizzaria.com` | `admin123` |
| USER | `user@pizzaria.com` | `user123` |

20 pizzas (14 salgadas + 6 doces), each with 4 tamanhos. Seeded by `DataInitializer`.

## Build

```bash
./mvnw clean package -DskipTests   # uber-JAR (target/cardapio-1.0.0.jar)
docker compose up -d --build       # full stack (app + PostgreSQL 16 + Redis 7)
```

Dockerfile uses `eclipse-temurin:17-jre-alpine`, copies pre-built JAR, activates `postgres` profile.

Flyway migration: `V1__init_schema.sql` (only active in `postgres` profile).

## Important constraints

- No `s` suffix on enum values: `Categoria.SALGADA` (not `SALGADAS`)
- Endpoints: `/api/pizzas` (not `/api/pizza`) — matches the table name convention
- H2 console only in `default` profile; disabled in `postgres`
- CORS origins explicit: localhost:3000, localhost:5173, 127.0.0.1:3000, 127.0.0.1:5173
- Rate limit: 5 login attempts / 5 min per IP
- Refresh token rotation: each refresh invalidates the previous token
- `RateLimitHeadersFilter` adds `X-RateLimit-Remaining` header on `POST /api/auth/login` responses
- Refresh tokens are rejected by `JwtAuthenticationFilter` — only access tokens can authenticate API requests
- `RateLimitExceededException` (not `IllegalStateException`) is thrown for rate limit violations

Graphify knowledge graph at `graphify-out/` — run `graphify query "<question>"` for architecture questions.