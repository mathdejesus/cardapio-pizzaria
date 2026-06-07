# Planejamento — Cardapio Digital

Auditoria completa do projeto com issues identificadas e ações sugeridas.

---

## Resumo do Projeto

API REST para cardápio digital de pizzaria, construída com Spring Boot 3.3.5 + Java 17.

| Camada | Tecnologia |
|--------|-----------|
| API | Spring Web + HATEOAS + SpringDoc OpenAPI |
| Auth | JWT (jjwt 0.12.6) + refresh token com rotação |
| Persistence | Spring Data JPA + H2 (dev) / PostgreSQL (prod) |
| Cache | Caffeine (dev) / Redis (prod), TTL 10min |
| Segurança | Rate limit 5/min por IP, blocklist, soft delete |
| Infra | Docker Compose (app + PostgreSQL 16 + Redis 7) |

Endpoints: CRUD de pizzas (admin), cardápio público paginado, login/refresh/logout, perfil do usuário. 55 testes automatizados.

---

## Issues Críticas (Segurança e Bugs)

### C1. Logout quebra TODOS os tokens ativos do sistema

**Arquivos:** `AuthService.java:95-104`, `JwtService.java:40-44`

Access tokens não têm claim `jti`. Quando alguém faz logout, `extractJti()` retorna `null`, e `addToBlocklist(null, ...)` é chamado. Na próxima requisição de **qualquer** usuário, `isBlocklisted(null)` retorna `true` → todos os access tokens validados são rejeitados.

**Correção:** Adicionar `jti` ao access token em `JwtService.generateToken()`, ou fazer logout apenas com refresh token.

### C2. Logout não revoga o refresh token

**Arquivo:** `AuthService.java:95-104`

O método `logout()` só adiciona o jti à blocklist (que é null — veja C1). Nunca chama `tokenStore.deleteRefreshToken()`. Após logout, o refresh token continua válido → usuário pode chamar `POST /api/auth/refresh` e obter novos tokens.

**Correção:** No `logout()`, extrair o jti do refresh token (não do access token) e chamar `deleteRefreshToken(jti)`.

### C3. JWT secret com fallback hardcoded no código

**Arquivo:** `application.properties:28`

```properties
jwt.secret=${JWT_SECRET:dev-secret-key-with-at-least-32-characters-here}
```

Se a env var não estiver definida (inclusive no profile `postgres`), qualquer pessoa que leia o fonte pode forjar tokens JWT. A validação no `@PostConstruct` (`JwtService.java:33`) só verifica tamanho (>=32), não se é o valor default.

**Correção:** Remover o fallback ou lançar exceção se `JWT_SECRET` não for definida.

### C4. Rate limit contornável via `X-Forwarded-For` spoofing

**Arquivo:** `IpUtils.java:15-17`

```java
String xfwd = request.getHeader("X-Forwarded-For");
if (xfwd != null && !xfwd.isBlank()) {
    return xfwd.split(",")[0].trim();
}
```

O header `X-Forwarded-For` é aceito sem validação. Um atacante pode rotacionar IPs falsos e bypassar completamente o rate limit de 5 tentativas/5min.

**Correção:** Usar apenas `request.getRemoteAddr()` ou configurar trusted proxies.

### C5. Soft delete sem recuperação — admin não consegue ver pizzas deletadas

**Arquivo:** `Pizza.java:18`, `PizzaService.java:88-91`

`@SQLRestriction("deleted = false")` filtra globalmente. Não existe endpoint para listar, buscar ou restaurar pizzas deletadas. A mensagem de erro "Pizza não encontrada" é enganosa — a pizza existe, mas está deletada.

**Correção:** Adicionar endpoint `GET /api/pizzas/deleted` (admin only) e `PATCH /api/pizzas/{id}/restore`.

---

## Issues Médias

| # | Issue | Arquivo | Correção sugerida |
|---|-------|---------|-------------------|
| M1 | `GET /api/pizzas/categoria/{categoria}` retorna pizzas indisponíveis para anônimos | `PizzaRepository.java:11` | Filtrar por `disponivel=true` ou adicionar query separada |
| M2 | Endpoint de categoria sem paginação (potencial DoS) | `PizzaController.java:49` | Adicionar `Pageable` como o endpoint de listagem |
| M3 | Header `X-RateLimit-Remaining` sempre defasado (mostra valor antes do request) | `RateLimitHeadersFilter.java:25` | Mover para afterCompletion ou recalcular |
| M4 | `InMemoryLoginRateLimiter` depende de constantes do `RedisLoginRateLimiter` | `InMemoryLoginRateLimiter.java:15` | Extrair constantes para interface `LoginRateLimiter` |
| M5 | Zero logging de eventos de segurança (login falhou, rate limit, logout) | Todos os services | Adicionar SLF4J em AuthService, RateLimiter |
| M6 | `InMemoryTokenStore` e `InMemoryLoginRateLimiter` crescem infinitamente em memória | `InMemoryTokenStore.java`, `InMemoryLoginRateLimiter.java` | Adicionar cleanup periódico ou max size |
| M7 | Race condition no `DataInitializer` com múltiplas instâncias | `DataInitializer.java:37` | Usar `@Transactional` + upsert ou `INSERT IGNORE` |
| M8 | `@Data` em entidades JPA gera `equals()/hashCode()` perigosos | `Pizza.java:19`, `Usuario.java:12` | Usar `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` |
| M9 | Índices faltando no Flyway: composite `(deleted, disponivel)`, `(deleted, categoria)` | `V1__init_schema.sql` | Adicionar índices compostos |
| M10 | `Usuario.senha` sem `@JsonIgnore` — risco de expor hash BCrypt | `Usuario.java:26` | Adicionar `@JsonIgnore` |

---

## Issues Baixas (Qualidade de Código)

| # | Issue | Arquivo |
|---|-------|---------|
| L1 | `UserService.hasRole()` é dead code — nunca chamado | `UserService.java:28-32` |
| L2 | `@ParameterObject` faltando para `Pageable` no Swagger | `PizzaController.java:33` |
| L3 | Sem versionamento de API (`/api/v1/pizzas`) | Todos os controllers |
| L4 | `PizzaMapper.toIngredientesFromDTO` cria cópia redundante | `PizzaMapper.java:33-35` |
| L5 | Métrica `cardapio.pizzas.total` inclui soft-deleted — nome enganoso | `CardapioMetrics.java:22-24` |
| L6 | Flyway `VARCHAR(255)` para `nome` vs DTO `@Size(max=100)` — mismatch | `V1__init_schema.sql:3`, `PizzaRequestDTO.java:23` |
| L7 | `@Column(precision, scale)` sem efeito em `@Embeddable` | `Tamanho.java:26` |
| L8 | Senhas de seed hardcoded no fonte (dev only, mas deve documentar) | `DataInitializer.java:43,48` |

---

## Features Faltando para Cardápio Digital

### Alta Prioridade

| Feature | Descrição |
|---------|-----------|
| Imagem/foto das pizzas | Sem fotos, o cardápio digital é incompleto para o cliente |
| Cadastro de usuário | `POST /api/auth/register` — hoje só existe seed data |
| Busca por ingrediente | "Pizzas com catupiry" — funcionalidade básica de cardápio |

### Média Prioridade

| Feature | Descrição |
|---------|-----------|
| Carrinho / Pedidos | API de cardápio sem pedidos é só metade do produto |
| Atualização de perfil | `PUT /api/auth/me` — nome, telefone, endereço |
| Favoritos / lista de desejos | Engajamento do cliente |
| Informação de alérgenos | Obligação legal em muitos contextos |

### Baixa Prioridade

| Feature | Descrição |
|---------|-----------|
| Preço promocional / cupons | Marketing e vendas |
| Horário de funcionamento | Disponibilidade temporal |
| Histórico de alterações | Audit trail para mudanças de preço |
| Suporte multi-idioma | i18n para turistas |

---

## Plano de Ação Sugerido

### Fase 1 — Segurança (urgente)

1. **[C1+C2] Corrigir logout** — adicionar `jti` ao access token, revogar refresh token no logout
2. **[C3] Remover fallback do JWT secret** — falhar se `JWT_SECRET` não estiver definida
3. **[C4] Corrigir rate limit** — parar de confiar em `X-Forwarded-For`
4. **[C5] Adicionar recuperação de soft delete** — endpoints de listagem e restore

### Fase 2 — Qualidade (próxima sprint)

5. **[M1-M3] Corrigir endpoints de categoria** — filtrar indisponíveis, adicionar paginação
6. **[M5] Adicionar logging** — eventos de segurança em todos os services
7. **[M8] Corrigir `@Data` nas entidades** — `@EqualsAndHashCode` com ID only
8. **[M9] Melhorar índices Flyway** — composite indexes para queries comuns

### Fase 3 — Features (produtos)

9. **Imagem das pizzas** — upload + URL no entity
10. **Cadastro de usuário** — endpoint público de registro
11. **Busca por ingrediente** — nova query no repository
12. **Carrinho e pedidos** — nova entidade `Pedido` com itens

---

## Referências

- `AGENTS.md` — instruções para agentes de código
- `README.md` — documentação completa da API
- `PLANEJAMENTO.md` — este arquivo
