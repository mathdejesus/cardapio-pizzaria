# Cardapio — Spring Boot REST API

Pizzaria menu API with JWT auth, soft delete, Redis cache, rate limiting, HATEOAS.

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

## Tests (55 total — no infra required)

```bash
./mvnw test                                         # all 55
./mvnw test -Dtest="*ServiceTest"                   # unit only (30)
./mvnw test -Dtest="*ControllerTest"                # integration only (8)
./mvnw test -Dtest="*StoreTest,*RateLimiterTest"    # infra unit tests (17)
./mvnw test -Dtest="PizzaServiceTest#testName"      # single method
```

H2 in-memory, Redis excluded. `TestConfig` mocks 5 beans: `LoginRateLimiter`, `TokenStore`, `CardapioMetrics`, `JwtService`, `UserDetailsService`. Tests use `application-test.properties` (`spring.cache.type=none`).

## Architecture quirks (agent will miss)

- **Soft delete**: `@SQLRestriction("deleted = false")` on `Pizza` entity — all JPA derived queries auto-filter deleted rows. If you add `@Query` methods in the future, you must add `WHERE deleted = false` manually.
- **Dual `List` collections on Pizza**: `tamanhos` + `ingredientes` → `@Fetch(FetchMode.SUBSELECT)` required to avoid Hibernate `MultipleBagFetchException`. Do NOT use `@EntityGraph` or JOIN FETCH here.
- **Dirty checking**: `PizzaService.update`/`updateDisponibilidade` do NOT call `save()` — JPA dirty checking flushes managed entities automatically within `@Transactional`.
- **MapStruct**: `PizzaMapper` generates at compile time via annotation processor. After changing mapper, run `./mvnw clean compile` before tests.
- **Cache**: `@Cacheable("cardapio")` on `CardapioService.getCardapio()`; all write methods in `PizzaService` do `@CacheEvict(value = "cardapio", allEntries = true)`. Profile `default` uses Caffeine, `postgres` uses Redis.
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

## Knowledge graph

A graphify knowledge graph is available at `graphify-out/`. For focused architecture questions, run `graphify query "<question>"` instead of grepping raw files.
