# Graph Report - .  (2026-05-31)

## Corpus Check
- Corpus is ~10,566 words - fits in a single context window. You may not need a graph.

## Summary
- 483 nodes · 953 edges · 43 communities (27 shown, 16 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 83 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_DTOs & Claims|DTOs & Claims]]
- [[_COMMUNITY_Core Domain|Core Domain]]
- [[_COMMUNITY_MapStruct Mapping|MapStruct Mapping]]
- [[_COMMUNITY_Data Initialization|Data Initialization]]
- [[_COMMUNITY_Exception Handling|Exception Handling]]
- [[_COMMUNITY_REST Controllers|REST Controllers]]
- [[_COMMUNITY_Service Layer|Service Layer]]
- [[_COMMUNITY_Test Infrastructure|Test Infrastructure]]
- [[_COMMUNITY_User Management|User Management]]
- [[_COMMUNITY_Auth Controller Tests|Auth Controller Tests]]
- [[_COMMUNITY_JWT Security|JWT Security]]
- [[_COMMUNITY_Security Config|Security Config]]
- [[_COMMUNITY_Cache Config|Cache Config]]
- [[_COMMUNITY_Rate Limiting|Rate Limiting]]
- [[_COMMUNITY_Token Storage|Token Storage]]
- [[_COMMUNITY_Flyway Migration|Flyway Migration]]
- [[_COMMUNITY_H2 Console|H2 Console]]
- [[_COMMUNITY_API Documentation|API Documentation]]
- [[_COMMUNITY_CORS Config|CORS Config]]
- [[_COMMUNITY_Actuator Security|Actuator Security]]
- [[_COMMUNITY_Metrics|Metrics]]
- [[_COMMUNITY_Pizza Repository|Pizza Repository]]
- [[_COMMUNITY_Pizza Service Tests|Pizza Service Tests]]
- [[_COMMUNITY_Cardapio Tests|Cardapio Tests]]
- [[_COMMUNITY_Auth Service Tests|Auth Service Tests]]
- [[_COMMUNITY_Service Tests|Service Tests]]
- [[_COMMUNITY_Integration Tests|Integration Tests]]
- [[_COMMUNITY_Unit Tests|Unit Tests]]
- [[_COMMUNITY_Request DTOs|Request DTOs]]
- [[_COMMUNITY_Response DTOs|Response DTOs]]
- [[_COMMUNITY_Enums|Enums]]
- [[_COMMUNITY_Maven Build|Maven Build]]
- [[_COMMUNITY_Model Layer|Model Layer]]
- [[_COMMUNITY_Repository Layer|Repository Layer]]
- [[_COMMUNITY_Auth Flow|Auth Flow]]
- [[_COMMUNITY_Refresh Token Flow|Refresh Token Flow]]
- [[_COMMUNITY_Blocklist Flow|Blocklist Flow]]
- [[_COMMUNITY_Seed Data|Seed Data]]
- [[_COMMUNITY_Validation|Validation]]

## God Nodes (most connected - your core abstractions)
1. `JwtService` - 15 edges
2. `GlobalExceptionHandler` - 13 edges
3. `ErrorResponse` - 13 edges
4. `ResponseEntity` - 12 edges
5. `PizzaServiceTest` - 12 edges
6. `ExceptionHandler` - 11 edges
7. `String` - 11 edges
8. `PizzaService` - 10 edges
9. `RedisLoginRateLimiter` - 9 edges
10. `Test` - 9 edges

## Surprising Connections (you probably didn't know these)
- `DataInitializer` --calls--> `PizzaRepository`  [EXTRACTED]
  src/main/java/com/pizzaria/config/DataInitializer.java → src/main/java/com/pizzaria/repository/PizzaRepository.java
- `InMemoryLoginRateLimiter` --semantically_similar_to--> `RedisLoginRateLimiter`  [INFERRED] [semantically similar]
  src/main/java/com/pizzaria/config/InMemoryLoginRateLimiter.java → src/main/java/com/pizzaria/config/RedisLoginRateLimiter.java
- `AuthService` --calls--> `CardapioMetrics`  [EXTRACTED]
  src/main/java/com/pizzaria/service/AuthService.java → src/main/java/com/pizzaria/metrics/CardapioMetrics.java
- `V1__init_schema (Flyway)` --conceptually_related_to--> `PizzaRepository`  [INFERRED]
  src/main/resources/db/migration/V1__init_schema.sql → src/main/java/com/pizzaria/repository/PizzaRepository.java
- `RedisTokenStore` --semantically_similar_to--> `InMemoryTokenStore`  [INFERRED] [semantically similar]
  src/main/java/com/pizzaria/security/RedisTokenStore.java → src/main/java/com/pizzaria/security/InMemoryTokenStore.java

## Import Cycles
- None detected.

## Communities (43 total, 16 thin omitted)

### Community 0 - "DTOs & Claims"
Cohesion: 0.11
Nodes (18): Claims, CardapioResponseDTO, Function, Map, Object, PostConstruct, SecretKey, JwtService (+10 more)

### Community 1 - "Core Domain"
Cohesion: 0.08
Nodes (28): CardapioMetrics, CardapioService, Pizza (Entity), PizzaController, PizzaMapper (MapStruct), PizzaRepository, PizzaService, Tamanho (Embeddable) (+20 more)

### Community 2 - "MapStruct Mapping"
Cohesion: 0.16
Nodes (14): PizzaMapper, Mapping, PizzaServiceTest, Pizza, PizzaRequestDTO, PizzaResponseDTO, Tamanho, Long (+6 more)

### Community 3 - "Data Initialization"
Cohesion: 0.11
Nodes (17): ApplicationArguments, ApplicationRunner, BigDecimal, DataInitializer, TamanhoRequestDTO, TamanhoResponseDTO, Tamanho, PasswordEncoder (+9 more)

### Community 4 - "Exception Handling"
Cohesion: 0.21
Nodes (14): AccessDeniedException, ErrorResponse, Exception, GlobalExceptionHandler, ExceptionHandler, FieldError, HttpMessageNotReadableException, IllegalArgumentException (+6 more)

### Community 5 - "REST Controllers"
Cohesion: 0.17
Nodes (18): PizzaController, DeleteMapping, EntityModel, PagedModel, PatchMapping, PutMapping, DisponibilidadeRequestDTO, GetMapping (+10 more)

### Community 6 - "Service Layer"
Cohesion: 0.20
Nodes (14): CacheEvict, CategoriaInvalidaException, PizzaNotFoundException, PizzaService, Categoria, DisponibilidadeRequestDTO, List, Long (+6 more)

### Community 7 - "Test Infrastructure"
Cohesion: 0.17
Nodes (14): BadCredentialsException, CardapioMetrics, TestConfig, JwtService, LoginRateLimiter, Primary, UserDetailsServiceImpl, UserDetails (+6 more)

### Community 8 - "User Management"
Cohesion: 0.13
Nodes (14): DataInitializer, Usuario (Entity), UsuarioRepository, UserProfileDTO, Usuario, UsuarioRepository, Role, UserService (+6 more)

### Community 9 - "Auth Controller Tests"
Cohesion: 0.14
Nodes (10): TestConfig, AuthControllerTest, CardapioControllerTest, PizzaControllerTest, HttpStatus, Page, RefreshTokenRequestDTO, Test (+2 more)

### Community 10 - "JWT Security"
Cohesion: 0.17
Nodes (14): JwtAuthenticationEntryPoint, AuthenticationConfiguration, AuthenticationEntryPoint, AuthenticationManager, AuthenticationProvider, SecurityConfig, JwtAuthenticationEntryPoint, Bean (+6 more)

### Community 11 - "Security Config"
Cohesion: 0.20
Nodes (9): ConditionalOnProperty, CacheConfig, RedisCacheManagerBuilderCustomizer, RedisTokenStore, Bean, Duration, Optional, Override (+1 more)

### Community 12 - "Cache Config"
Cohesion: 0.23
Nodes (12): UserService, AuthController, AuthResponseDTO, GetMapping, LoginRequestDTO, PostMapping, ResponseEntity, SecurityRequirement (+4 more)

### Community 13 - "Rate Limiting"
Cohesion: 0.19
Nodes (12): RateLimitHeadersFilter, OncePerRequestFilter, JwtAuthenticationFilter, FilterChain, HttpServletRequest, HttpServletResponse, Override, FilterChain (+4 more)

### Community 14 - "Token Storage"
Cohesion: 0.30
Nodes (5): AttemptWindow, InMemoryLoginRateLimiter, Instant, Override, String

### Community 15 - "Flyway Migration"
Cohesion: 0.19
Nodes (12): AuthController, AuthService, InMemoryLoginRateLimiter, InMemoryTokenStore, JwtAuthenticationFilter, JwtService, LoginRateLimiter (interface), RateLimitHeadersFilter (+4 more)

### Community 16 - "H2 Console"
Cohesion: 0.35
Nodes (6): InMemoryTokenStore, isExpired(), Duration, Optional, Override, String

### Community 17 - "API Documentation"
Cohesion: 0.47
Nodes (3): RedisLoginRateLimiter, Override, String

### Community 18 - "CORS Config"
Cohesion: 0.36
Nodes (4): TokenStore, Duration, Optional, String

### Community 19 - "Actuator Security"
Cohesion: 0.22
Nodes (5): CategoriaInvalidaException, PizzaNotFoundException, RuntimeException, String, Long

### Community 20 - "Metrics"
Cohesion: 0.48
Nodes (5): ActuatorSecurityConfig, Order, Bean, HttpSecurity, SecurityFilterChain

### Community 22 - "Pizza Service Tests"
Cohesion: 0.60
Nodes (3): CorsConfig, CorsFilter, Bean

### Community 23 - "Cardapio Tests"
Cohesion: 0.60
Nodes (3): OpenApiConfig, OpenAPI, Bean

## Knowledge Gaps
- **58 isolated node(s):** `String`, `Override`, `String`, `PizzaSeed`, `Override` (+53 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **16 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Map` connect `DTOs & Claims` to `H2 Console`, `Auth Controller Tests`, `Token Storage`, `Core Domain`?**
  _High betweenness centrality (0.121) - this node is a cross-community bridge._
- **Why does `HttpStatus` connect `Auth Controller Tests` to `Exception Handling`, `REST Controllers`?**
  _High betweenness centrality (0.094) - this node is a cross-community bridge._
- **Why does `PizzaRepository` connect `Core Domain` to `MapStruct Mapping`, `Data Initialization`, `Service Layer`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **What connects `String`, `Override`, `String` to the rest of the system?**
  _58 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `DTOs & Claims` be split into smaller, more focused modules?**
  _Cohesion score 0.1091581868640148 - nodes in this community are weakly interconnected._
- **Should `Core Domain` be split into smaller, more focused modules?**
  _Cohesion score 0.07665505226480836 - nodes in this community are weakly interconnected._
- **Should `Data Initialization` be split into smaller, more focused modules?**
  _Cohesion score 0.10804597701149425 - nodes in this community are weakly interconnected._