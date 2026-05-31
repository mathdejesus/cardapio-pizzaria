# Cardapio - API REST de Pizzaria

API REST para gerenciamento de cardápio de pizzaria com autenticação JWT, soft delete, cache, paginação e rate limiting.

## Stack

| Tecnologia | Versão |
|---|---|---|
| Java | 17+ |
| Spring Boot | 3.3.5 |
| Spring Security | 6.x |
| Spring Data JPA / Hibernate | 6.5.3 |
| Spring Cache | Redis (TTL 10 min) |
| Spring Data Redis | gerenciado |
| H2 Database | dev (in-memory) |
| PostgreSQL | production |
| JWT (jjwt) | 0.12.6 |
| SpringDoc OpenAPI | 2.6.0 |
| Lombok | Última |
| MapStruct | 1.5.5.Final |
| Jakarta Validation | Hibernate Validator |
| Maven | Wrapper incluso |

## Arquitetura

```
PizzariaApplication (@EnableCaching)
  |
  +-- config/          --> SecurityConfig, CorsConfig, RedisRateLimiter,
  |                        CacheConfig, RateLimitHeadersFilter,
  |                        DataInitializer, OpenApiConfig
  |
  +-- controller/      --> AuthController     → AuthService
  |                        CardapioController  → CardapioService
  |                        PizzaController     → PizzaService
  |
  +-- service/         --> AuthService         → Auth (rate limit + JWT + Redis)
  |                        CardapioService     → consulta + cache
  |                        PizzaService        → CRUD + soft delete
  |
  +-- repository/      --> PizzaRepository     → Pizza (JPA)
  |                        UsuarioRepository   → Usuario (JPA)
  |
  +-- model/           --> Pizza (@Entity)     → Tamanho (@Embeddable)
  |                        Usuario (@Entity)
  |
  +-- dto/             --> Request → PizzaRequestDTO, TamanhoRequestDTO,
  |                                  LoginRequestDTO, RefreshTokenRequestDTO,
  |                                  DisponibilidadeRequestDTO
  |                        Response → PizzaResponseDTO, CardapioResponseDTO,
  |                                    AuthResponseDTO
  |
  +-- security/        --> JwtService, JwtAuthenticationFilter,
  |                        JwtAuthenticationEntryPoint, UserDetailsServiceImpl
  |
  +-- mapper/          --> PizzaMapper (MapStruct)
  |
  +-- exception/       --> GlobalExceptionHandler (@RestControllerAdvice)
  |                        ErrorResponse, PizzaNotFoundException,
  |                        CategoriaInvalidaException
  |
  +-- enums/           --> Categoria { SALGADA, DOCE }
                           TamanhoTipo { PEQUENA, MEDIA, GRANDE, FAMILIA }
                           Role { ADMIN }
```

### Fluxo de requisição

```
Cliente → CorsFilter → SecurityFilterChain → RateLimitHeadersFilter
  → JwtAuthenticationFilter (blocklist check + JWT parse)
  → Controller → Service → Repository → Database
  → Redis (cache / rate limiter / blocklist / refresh tokens)
                                         ↓
                               GlobalExceptionHandler (se erro)
```

### Fluxo de autenticação

```
POST /api/auth/login
  1. Rate limit check (Redis sliding window, 5/min por IP)
  2. AuthenticationManager.authenticate()
  3. Gera access token (JWT HMAC-SHA, 24h) + refresh token (7d)
  4. Armazena refresh token no Redis (TTL 7d)
  5. Retorna { accessToken, refreshToken, tokenType, expiresIn }

POST /api/auth/refresh
  1. Valida refresh token (JWT + Redis lookup)
  2. Verifica blocklist
  3. Rotaciona: invalida refresh antigo, gera novo par
  4. Retorna { accessToken, refreshToken, ... }

POST /api/auth/logout
  1. Extrai JTI do token
  2. Adiciona à blocklist no Redis (TTL = tempo restante do token)
  3. Retorna 204

Requisições autenticadas:
  Authorization: Bearer <token>
  1. JwtAuthenticationFilter checa blocklist no Redis
  2. Extrai username do token
  3. Carrega UserDetails do banco
  4. Valida token (subject + expiry)
  5. Seta SecurityContext
```

## Modelo de Dados

### Pizza (`pizzas`)

| Campo | Tipo | Observação |
|---|---|---|
| id | Long (PK) | Auto-increment |
| nome | String | NOT NULL |
| descricao | String | NOT NULL, max 1000 |
| categoria | Categoria (enum) | SALGADA / DOCE |
| disponivel | boolean | Default true |
| deleted | boolean | Soft delete, default false |
| tamanhos | List\<Tamanho\> | ElementCollection, FetchMode.SUBSELECT |
| ingredientes | List\<String\> | ElementCollection, FetchMode.SUBSELECT |

- `@SQLRestriction("deleted = false")` filtra globalmente registros deletados
- Duas `List` collections exigem `@Fetch(FetchMode.SUBSELECT)` em vez de `@EntityGraph` para evitar `MultipleBagFetchException` do Hibernate

### Tamanho (Embeddable — `pizza_tamanhos`)

| Campo | Tipo | Validação |
|---|---|---|
| tipo | TamanhoTipo (enum) | @NotNull |
| preco | BigDecimal(10,2) | @Positive, @Digits(8,2) |
| fatias | Integer | @NotNull, @Positive |

### Usuario (`usuarios`)

| Campo | Tipo | Observação |
|---|---|---|
| id | Long (PK) | Auto-increment |
| email | String | NOT NULL, unique |
| senha | String | BCrypt |
| role | Role (enum) | ADMIN, USER |

## Endpoints

### Públicos (sem autenticação)

| Método | Path | Descrição |
|---|---|---|
| POST | `/api/auth/login` | Autenticação (email + senha) → access + refresh tokens |
| POST | `/api/auth/refresh` | Renovar access token via refresh token (rotação) |
| GET | `/api/cardapio` | Cardápio completo (agrupado por categoria) |
| GET | `/api/pizzas` | Lista paginada de pizzas |
| GET | `/api/pizzas/{id}` | Pizza por ID |
| GET | `/api/pizzas/categoria/{categoria}` | Filtro por categoria (SALGADA / DOCE) |

### Autenticados (ADMIN ou USER)

| Método | Path | Descrição |
|---|---|---|
| GET | `/api/auth/me` | Perfil do usuário autenticado |
| POST | `/api/auth/logout` | Invalidar token (blocklist) |

### Autenticados (role ADMIN)

| Método | Path | Descrição |
|---|---|---|
| POST | `/api/pizzas` | Criar pizza |
| PUT | `/api/pizzas/{id}` | Atualizar pizza |
| DELETE | `/api/pizzas/{id}` | Soft delete |
| PATCH | `/api/pizzas/{id}/disponibilidade` | Alternar disponibilidade |

### Documentação

| Método | Path | Descrição |
|---|---|---|
| GET | `/swagger-ui.html` | Swagger UI |
| GET | `/api-docs` | OpenAPI JSON |
| GET | `/h2-console` | Console H2 (dev apenas) |
| GET | `/actuator/health` | Health check |
| GET | `/actuator/metrics` | Métricas Micrometer |
| GET | `/actuator/prometheus` | Métricas formato Prometheus |

### Paginação e HATEOAS

`GET /api/pizzas?page=0&size=10&sort=nome,asc`

Parâmetros `Pageable` do Spring Data aceitos: `page`, `size`, `sort`.

A resposta paginada inclui links HAL (`self`, `next`, `prev`) e cada pizza possui link `self`.

### Cache

- `@Cacheable("cardapio")` em `CardapioService.getCardapio()`
- `@CacheEvict(value = "cardapio", allEntries = true)` em todos os métodos de escrita de `PizzaService`
- **Dev (default):** cache local **Caffeine** (TTL 10 min) — Redis não é obrigatório
- **Produção (`postgres`):** cache **Redis** com TTL de 10 minutos
- `CacheConfig.java` customiza TTL no profile postgres via `RedisCacheManagerBuilderCustomizer`

### Rate Limiter

- Limite: **5 tentativas** de login por IP a cada **5 minutos**
- **Dev:** contador em memória (`InMemoryLoginRateLimiter`)
- **Produção:** Redis (`RedisLoginRateLimiter`) com `INCR` + `EXPIRE` de 5 minutos
- Headers de resposta: `X-RateLimit-Remaining`

### Refresh Token & Logout

- Refresh token com **7 dias** de validade (Redis em produção, memória em dev)
- Rota `POST /api/auth/refresh` com rotação (invalida o token anterior)
- Logout via `POST /api/auth/logout` adiciona o JTI à blocklist no Redis
- `JwtAuthenticationFilter` verifica blocklist em toda requisição autenticada

## Tratamento de Erros

| Exceção | HTTP | Mensagem |
|---|---|---|
| PizzaNotFoundException | 404 | "Pizza nao encontrada com id: N" |
| CategoriaInvalidaException | 400 | "Categoria invalida: X. Use SALGADA ou DOCE." |
| MethodArgumentNotValidException | 400 | "Erro de validacao nos dados enviados" + erros por campo |
| HttpMessageNotReadableException | 400 | "JSON invalido ou malformado" |
| BadCredentialsException | 401 | "Credenciais invalidas" |
| AuthenticationException | 401 | "Credenciais invalidas" |
| AccessDeniedException | 403 | "Acesso negado" |
| IllegalArgumentException | 400 | Refresh token inválido/expirado/revogado |
| IllegalStateException | 429 | Rate limiter (muitas tentativas) |
| Exception (genérica) | 500 | "Erro interno do servidor" |

Resposta padrão:

```json
{
  "timestamp": "2026-05-28T23:00:00",
  "status": 400,
  "message": "Erro de validacao nos dados enviados",
  "errors": ["nome: O nome é obrigatório"]
}
```

## Configuração

### Variáveis de ambiente (obrigatórias)

| Variável | Obrigatória | Descrição |
|---|---|---|
| `JWT_SECRET` | Sim | String com 32+ caracteres para assinar tokens |

### Variáveis de ambiente (Redis)

| Variável | Default | Descrição |
|---|---|---|
| `SPRING_DATA_REDIS_HOST` | localhost | Host do Redis |
| `SPRING_DATA_REDIS_PORT` | 6379 | Porta do Redis |

### Variáveis de ambiente (profile postgres)

| Variável | Default | Descrição |
|---|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/pizzaria` | URL do PostgreSQL |
| `DATABASE_USER` | `postgres` | Usuário do banco |
| `DATABASE_PASSWORD` | `postgres` | Senha do banco |

### Profiles

- **default**: H2 em memória, cache Caffeine, sem Redis, `ddl-auto=create-drop`, Flyway desabilitado
- **postgres**: PostgreSQL + Redis + Flyway (`ddl-auto=validate`)

Ativar com: `--spring.profiles.active=postgres`

### Métricas customizadas

| Métrica | Descrição |
|---|---|
| `cardapio.login.attempts` | Total de tentativas de login |
| `cardapio.login.failures` | Logins com credenciais inválidas |
| `cardapio.pizzas.total` | Quantidade de pizzas no banco |

Métricas de cache (`cache.*`) expostas automaticamente via Actuator quando o cache está ativo.

### CORS (origens permitidas)

- `http://localhost:3000`
- `http://localhost:5173`
- `http://127.0.0.1:3000`
- `http://127.0.0.1:5173`

### Seed Data (DataInitializer)

- **Admin**: `admin@pizzaria.com` / `admin123`
- **User**: `user@pizzaria.com` / `user123` (role USER — leitura de perfil e logout)
- **20 pizzas**: 14 salgadas + 6 doces, cada uma com 4 tamanhos

## Como executar

### Pré-requisitos

- **Java 17+** (JDK)
- **Maven** (ou use o wrapper `./mvnw` incluso)
- **Docker + Docker Compose** (apenas para o profile `postgres`)

---

### 1. Desenvolvimento local (H2 + Caffeine — sem Redis)

Modo mais simples, sem dependências externas. Usa banco H2 em memória e cache Caffeine.

```bash
# 1. Defina a chave JWT (obrigatório, mínimo 32 caracteres)
export JWT_SECRET="minha-chave-super-segura-com-pelo-menos-32-caracteres!"

# 2. Execute a aplicação
./mvnw spring-boot:run
```

A aplicação iniciará em `http://localhost:8080`.

---

### 2. Produção local (Docker Compose — PostgreSQL + Redis + Flyway)

Sobe a aplicação com PostgreSQL 16, Redis 7 e Flyway para migrações.

```bash
# 1. (Opcional) Defina a JWT_SECRET — se não definir, usa o fallback do docker-compose.yml
export JWT_SECRET="minha-chave-super-segura-com-pelo-menos-32-caracteres!"

# 2. Build e execute os containers
docker compose up -d --build

# 3. Acompanhe os logs
docker compose logs -f app
```

A aplicação estará disponível em `http://localhost:8080` após o healthcheck passar (~60s).

Para derrubar:
```bash
docker compose down -v   # -v remove o volume do PostgreSQL
```

---

### 3. Apenas banco + Redis (para rodar a app via Maven com profile postgres)

Use quando quiser a aplicação rodando via Maven mas com PostgreSQL/Redis de verdade.

```bash
# Sobe apenas PostgreSQL e Redis
docker compose up -d db redis

# Aguarda o PostgreSQL ficar pronto
docker compose exec db pg_isready -U postgres -d pizzaria

# Executa a aplicação com o profile postgres
export JWT_SECRET="minha-chave-super-segura-com-pelo-menos-32-caracteres!"
export DATABASE_URL=jdbc:postgresql://localhost:5432/pizzaria
export DATABASE_USER=postgres
export DATABASE_PASSWORD=postgres
export SPRING_DATA_REDIS_HOST=localhost
export SPRING_DATA_REDIS_PORT=6379

./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

---

## Testes

### Stack de testes

| Ferramenta | Uso |
|---|---|
| JUnit 5 | Runner |
| Mockito | Mocks e stubs |
| AssertJ | Assertivas fluentes |
| TestRestTemplate | Chamadas HTTP nos testes de integração |
| H2 (in-memory) | Banco de testes (sem PostgreSQL/Redis) |
| `@TestConfiguration` | Mocks de infraestrutura (JWT, Redis, rate limiter) |

### Estrutura

```
src/test/
├── resources/
│   └── application-test.properties   # H2 + cache none + JWT mock
└── java/com/pizzaria/
    ├── config/
    │   └── TestConfig.java           # @Primary mocks (5 beans)
    ├── controller/                   # Integração (@SpringBootTest + REST)
    │   ├── AuthControllerTest.java   #   3 testes
    │   ├── CardapioControllerTest.java # 1 teste
    │   └── PizzaControllerTest.java  #   4 testes
    └── service/                      # Unitários (Mockito puro)
        ├── AuthServiceTest.java      #   6 testes
        ├── CardapioServiceTest.java  #   2 testes
        └── PizzaServiceTest.java     #   8 testes
```

**Total: 24 testes** (16 unitários + 8 de integração).

### Service (testes unitários)

Usam `@ExtendWith(MockitoExtension.class)` com `@Mock` + `@InjectMocks`. Não carregam o Spring context.

| Classe | Testes | O que cobre |
|---|---|---|
| **AuthServiceTest** | 6 | Login feliz, credenciais inválidas, refresh token (rotação, blocklist, tipo inválido), logout |
| **PizzaServiceTest** | 8 | CRUD completo: findAll paginado, findById (existente/inexistente), findByCategoria, create, update, softDelete, toggle disponibilidade |
| **CardapioServiceTest** | 2 | Cardápio agrupado por categoria com/sem pizzas disponíveis |

### Controller (testes de integração)

Usam `@SpringBootTest(webEnvironment = RANDOM_PORT)` com `TestRestTemplate` e `@MockBean` nos services. Importam `TestConfig.class` para mockar JWT, Redis e rate limiter.

| Classe | Testes | O que cobre |
|---|---|---|
| **AuthControllerTest** | 3 | POST login → 200 com tokens, POST refresh → 200, POST logout sem auth → 401 |
| **PizzaControllerTest** | 4 | GET listagem paginada → 200, GET por ID → 200, GET por categoria → 200, POST sem auth → 401 |
| **CardapioControllerTest** | 1 | GET /api/cardapio → 200 com categorias agrupadas |

### Executando os testes

```bash
# Todos os testes (24)
./mvnw test

# Apenas unitários (service)
./mvnw test -Dtest="*ServiceTest"

# Apenas integração (controller)
./mvnw test -Dtest="*ControllerTest"

# Classe específica
./mvnw test -Dtest=PizzaServiceTest

# Método específico
./mvnw test -Dtest=PizzaServiceTest#findById_shouldReturnPizzaWhenExists

# Com logs de SQL
./mvnw test -Dspring.jpa.show-sql=true

# Relatório de cobertura (gerado pelo surefire em target/surefire-reports/)
ls target/surefire-reports/*.txt
```

> ⚠️ Os testes não exigem Redis, PostgreSQL nem Docker. Usam H2 em memória e mocks para toda infraestrutura externa.

---

### Credenciais de teste (seed automático)

| Papel | Email | Senha |
|---|---|---|
| ADMIN | `admin@pizzaria.com` | `admin123` |
| USER | `user@pizzaria.com` | `user123` |

---

### Health check

```bash
curl http://localhost:8080/actuator/health
```

---

### Documentação da API

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs
- **Console H2 (dev apenas):** http://localhost:8080/h2-console

## Correções Realizadas

### P0 — Segurança (crítico)
- [x] JWT secret sem fallback — valida em startup que a env var tem 32+ chars
- [x] CORS com origens explícitas (sem `*`)
- [x] 401 JSON em token inválido/expirado (em vez de silencioso)

### P1 — Performance + Segurança
- [x] H2 console removido de produção
- [x] Frame options `sameOrigin`
- [x] Soft delete com `@SQLRestriction`
- [x] Nomes de métodos simplificados no repository
- [x] `@Fetch(FetchMode.SUBSELECT)` nas collections (fix MultipleBagFetchException)
- [x] Rate limiter (5 tentativas / 5 min por IP)
- [x] Dirty checking em vez de `save()` explícito

### P2 — Qualidade
- [x] GlobalExceptionHandler com catch-all Exception
- [x] `@SecurityRequirement` em nível de classe
- [x] Paginação com Pageable nos GETs
- [x] `@EnableCaching` + `@Cacheable`/`@CacheEvict`
- [x] Validação de DTO: `Integer fatias` + `@Digits` no preco
- [x] `save()` removido de update/updateDisponibilidade (dirty checking)

### P3 — Cache
- [x] **Migrar cache para Redis** — cache distribuído via `spring-boot-starter-data-redis`
- [x] **TTL configurável no cache** — `CacheConfig.java` com `RedisCacheManagerBuilderCustomizer` e TTL de 10 min

### P4 — Segurança (Redis)
- [x] **Refresh token** — implementado `POST /api/auth/refresh` com rotação (7d TTL no Redis)
- [x] **Logout** — implementado `POST /api/auth/logout` com blocklist JTI no Redis
- [x] **Rate limiter com Redis** — sliding window via `INCR` + `EXPIRE` no Redis
- [x] **Rate limit header** — header `X-RateLimit-Remaining` nas respostas de login

### P5 — Qualidade e Manutenibilidade
- [x] **MapStruct para DTOs** — `PizzaMapper` substitui mapeamento manual em `PizzaService` e `CardapioService`
- [x] **Testes automatizados** — 16 testes unitários (JUnit + Mockito) + 8 testes de integração (SpringBootTest + TestRestTemplate)

### P6 — Infraestrutura
- [x] **Dockerfile** — imagem `eclipse-temurin:17-jre-alpine`
- [x] **docker-compose** — orquestração com app + PostgreSQL 16 + Redis 7

## Melhorias recentes (relatório de sessão)

### P0
- [x] Rate limiter alinhado: **5 tentativas / 5 minutos** por IP
- [x] Fallback dev sem Redis: cache **Caffeine**, rate limit e tokens em memória

### P1
- [x] **Actuator** — `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`
- [x] **Métricas** — `cardapio.login.attempts`, `cardapio.login.failures`, `cardapio.pizzas.total`
- [x] **Flyway** — migration `V1__init_schema.sql` no profile `postgres` (`ddl-auto=validate`)

### P2
- [x] **HATEOAS** — links HAL na listagem paginada e no detalhe da pizza
- [x] **Role USER** — `user@pizzaria.com` + endpoint `GET /api/auth/me`
- [x] **Docker healthcheck** — `curl -f http://localhost:8080/actuator/health`
