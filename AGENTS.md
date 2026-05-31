# Cardapio — Spring Boot REST API

Pizzaria menu API with JWT auth, soft delete, Redis cache, rate limiting, HATEOAS.

## Quick start (dev, no infra needed)

```bash
export JWT_SECRET="minha-chave-super-segura-com-pelo-menos-32-caracteres!"
./mvnw spring-boot:run
```

H2 console at `/h2-console`, cache Caffeine, no Redis. Seed data auto-loaded (see DataInitializer).

## Profiles

| Profile | DB | Cache | Rate limiter | Flyway |
|---------|----|-------|-------------|--------|
| default | H2 in-mem | Caffeine | InMemory | disabled |
| postgres | PostgreSQL | Redis | Redis | enabled (`ddl-auto=validate`) |

Activate: `./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres`

## Tests (24 total — no infra required)

```bash
./mvnw test                                         # all 24
./mvnw test -Dtest="*ServiceTest"                   # unit only (16)
./mvnw test -Dtest="*ControllerTest"                # integration only (8)
./mvnw test -Dtest="PizzaServiceTest#testName"      # single method
```

H2 in-memory, Redis excluded via `@SpringBootTest` + `TestConfig`. All infra mocked.

## Architecture quirks (agent will miss)

- **Soft delete**: `@SQLRestriction("deleted = false")` on `Pizza` entity — all JPA queries auto-filter. Bulk repo methods (`@Query`) must add `WHERE deleted = false` manually.
- **Dual `List` collections on Pizza**: `tamanhos` + `ingredientes` → `@Fetch(FetchMode.SUBSELECT)` required to avoid Hibernate `MultipleBagFetchException`. Do NOT use `@EntityGraph` or JOIN FETCH here.
- **Dirty checking**: `PizzaService.update`/`toggleDisponibilidade` do NOT call `save()` — JPA dirty checking flushes managed entities automatically.
- **MapStruct**: `PizzaMapper` generates at compile time via annotation processor. After changing mapper, run `./mvnw clean compile` before tests.
- **Cache**: `@Cacheable("cardapio")` on `CardapioService.getCardapio()`; all write methods in `PizzaService` do `@CacheEvict(value = "cardapio", allEntries = true)`. Profile `default` uses Caffeine, `postgres` uses Redis.

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

20 pizzas (14 salgadas + 6 doces), each with 4 tamanhos.

## Build

```bash
./mvnw clean package -DskipTests   # uber-JAR
docker compose up -d --build       # full stack
```

## Important constraints

- No `s` suffix on enum values: `Categoria.SALGADA` (not `SALGADAS`)
- Endpoints: `/api/pizzas` (not `/api/pizza`) — matches the table name convention
- H2 console only in `default` profile; disabled in `postgres`
- CORS origins explicit: localhost:3000, localhost:5173, 127.0.0.1:3000, 127.0.0.1:5173
- Rate limit: 5 login attempts / 5 min per IP
- Refresh token rotation: each refresh invalidates the previous token
