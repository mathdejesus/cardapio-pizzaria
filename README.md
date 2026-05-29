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
| role | Role (enum) | ADMIN |

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

### Autenticados (role ADMIN)

| Método | Path | Descrição |
|---|---|---|
| POST | `/api/auth/logout` | Invalidar token (blocklist) |
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

### Paginação

`GET /api/pizzas?page=0&size=10&sort=nome,asc`

Parâmetros `Pageable` do Spring Data aceitos: `page`, `size`, `sort`.

### Cache

- `@Cacheable("cardapio")` em `CardapioService.getCardapio()`
- `@CacheEvict(value = "cardapio", allEntries = true)` em todos os métodos de escrita de `PizzaService`
- Cache distribuído via **Redis** com TTL configurável de **10 minutos**
- `CacheConfig.java` customiza TTL por cache via `RedisCacheManagerBuilderCustomizer`

### Rate Limiter

- Sliding window no Redis (`INCR` + `EXPIRE` por minuto)
- Limite: **5 tentativas** de login por IP a cada **5 minutos**
- Headers de resposta: `X-RateLimit-Remaining`

### Refresh Token & Logout

- Refresh token com **7 dias** de validade, armazenado no Redis
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

- **default**: H2 em memória, `ddl-auto=create-drop`
- **postgres**: PostgreSQL, `ddl-auto=update`

Ativar com: `--spring.profiles.active=postgres`

### CORS (origens permitidas)

- `http://localhost:3000`
- `http://localhost:5173`
- `http://127.0.0.1:3000`
- `http://127.0.0.1:5173`

### Seed Data (DataInitializer)

- **Admin**: `admin@pizzaria.com` / `admin123`
- **20 pizzas**: 14 salgadas + 6 doces, cada uma com 4 tamanhos

## Como executar

```bash
# Desenvolvimento (H2 + Redis)
# Pré-requisito: Redis rodando em localhost:6379
docker run -d -p 6379:6379 redis:7-alpine

export JWT_SECRET="minha-chave-super-segura-com-pelo-menos-32-caracteres!"
./mvnw spring-boot:run

# Produção (Docker Compose — PostgreSQL + Redis)
export JWT_SECRET="..."
docker compose up -d

# Testes
./mvnw test
```

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

## Pendências (ainda não realizadas)

### Observabilidade
- [ ] **Health check** — adicionar `spring-boot-starter-actuator` com endpoint `/actuator/health`
- [ ] **Métricas** — expor métricas (contagem de pizzas, cache hits/misses, login attempts)

### Infraestrutura
- [ ] **Migration com Flyway** — substituir `ddl-auto=update` por migrations versionadas

### API
- [ ] **HATEOAS links** — adicionar links de navegação nas respostas paginadas
