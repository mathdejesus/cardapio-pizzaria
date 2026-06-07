# Relatório de Sessão — Análise do Repositório Cardapio

**Data:** 30/05/2026
**Repositório:** `/home/matheus/IdeaProjects/Cardapio`

---

## 1. Estrutura do Projeto

```
Cardapio/
├── pom.xml                          # Maven (Spring Boot 3.3.5, Java 17)
├── Dockerfile                       # eclipse-temurin:17-jre-alpine
├── docker-compose.yml               # app + PostgreSQL 16 + Redis 7
├── mvnw / mvnw.cmd                  # Maven Wrapper
├── AGENTS.md                        # Config de skills para IA
├── README.md                        # Documentação completa
├── src/main/java/com/pizzaria/
│   ├── PizzariaApplication.java
│   ├── config/          (7 arquivos)
│   ├── controller/      (3 arquivos)
│   ├── service/         (3 arquivos)
│   ├── repository/      (2 arquivos)
│   ├── model/           (3 arquivos)
│   ├── dto/             (8 arquivos)
│   ├── mapper/          (1 arquivo)
│   ├── security/        (4 arquivos)
│   ├── exception/       (4 arquivos)
│   └── enums/           (3 arquivos)
├── src/main/resources/
│   ├── application.properties
│   └── application-postgres.properties
└── src/test/java/com/pizzaria/
    ├── config/     TestConfig.java
    ├── controller/ AuthControllerTest.java
    │               CardapioControllerTest.java
    │               PizzaControllerTest.java
    └── service/    AuthServiceTest.java
                    CardapioServiceTest.java
                    PizzaServiceTest.java
```

---

## 2. Stack Atual

| Tecnologia | Versão |
|---|---|
| Java | 17+ |
| Spring Boot | 3.3.5 |
| Spring Security | 6.x |
| Spring Data JPA / Hibernate | 6.5.3 |
| Spring Cache (Redis) | TTL 10 min |
| Spring Data Redis | Gerenciado |
| H2 (dev) / PostgreSQL (prod) | — |
| JWT (jjwt) | 0.12.6 |
| SpringDoc OpenAPI | 2.6.0 |
| Lombok | Última |
| MapStruct | 1.5.5.Final |
| Jakarta Validation | Hibernate Validator |
| Maven Wrapper | Incluso |

---

## 3. Testes

- **16 testes unitários** (JUnit 5 + Mockito)
  - `AuthServiceTest` — 5 testes
  - `CardapioServiceTest` — 2 testes
  - `PizzaServiceTest` — 7 testes
- **8 testes de integração** (SpringBootTest + TestRestTemplate)
  - `AuthControllerTest` — 3 testes
  - `CardapioControllerTest` — 1 teste
  - `PizzaControllerTest` — 4 testes

---

## 4. Verificação das Pendências

Conforme README.md (seção "Pendências (ainda não realizadas)"), estas são as 4 pendências documentadas. **Todas continuam não implementadas:**

### 4.1 Health Check
- **Descrição:** Adicionar `spring-boot-starter-actuator` com endpoint `/actuator/health`
- **Status:** ❌ Não realizado
- **Evidência:** `pom.xml` não contém a dependência `spring-boot-starter-actuator`. Nenhum endpoint de health check existe.
- **Impacto:** Sem health check, não há como monitorar a saúde da aplicação em orquestradores (Kubernetes, Docker Compose health checks, etc.)

### 4.2 Métricas
- **Descrição:** Expor métricas (contagem de pizzas, cache hits/misses, login attempts)
- **Status:** ❌ Não realizado
- **Evidência:** Nenhuma configuração de Micrometer ou métricas customizadas. Não há beans de `MeterRegistry` ou `MeterBinder`.
- **Impacto:** Sem observabilidade sobre o comportamento da aplicação em produção.

### 4.3 Flyway Migrations
- **Descrição:** Substituir `ddl-auto=update` por migrations versionadas
- **Status:** ❌ Não realizado
- **Evidência:** Nenhuma dependência `flyway-core` ou `flyway-database-postgresql` no `pom.xml`. Diretório `db/migration/` não existe. Schema gerenciado via `ddl-auto=update/create-drop`.
- **Impacto:** `ddl-auto=update` é considerado má prática para produção — não há versionamento controlado do schema do banco.

### 4.4 HATEOAS Links
- **Descrição:** Adicionar links de navegação nas respostas paginadas
- **Status:** ❌ Não realizado
- **Evidência:** Nenhuma dependência `spring-hateoas` no `pom.xml`. Nenhum `WebMvcLinkBuilder` ou `EntityModel` sendo usado nos controllers.
- **Impacto:** Baixo — é um refinamento de API REST. Clientes precisam montar URLs de navegação manualmente.

---

## 5. Observações Adicionais

### 5.1 Inconsistência Rate Limiter
O README (linha 192) afirma:
> **5 tentativas** de login por IP a cada **5 minutos**

Porém a implementação em `RedisRateLimiter.java` usa uma chave com granularidade **por minuto** (`rate_limit:<ip>:<epoch_minute>`), o que efetivamente cria uma janela de 1 minuto, não 5. Há uma discrepância entre documentação e código.

### 5.2 Dependência de Redis
A aplicação exige Redis rodando mesmo no profile `default` (dev local). Não há fallback para cache local. O profile de teste (`application-test.properties`) usa `spring.cache.type=none` e mocks, mas o dev profile precisa de Redis ativo.

### 5.3 Role Única
Apenas a role `ADMIN` existe no enum `Role.java`. Não há role `USER` ou endpoints para usuários não-admin autenticados.

---

## 6. Próximos Passos Sugeridos

### Prioridade Alta (P0)
1. **Corrigir a documentação do rate limiter** — alinhar a descrição no README com a implementação real (5/min ou alterar a implementação para 5/5min)
2. **Adicionar fallback Redis no dev profile** — cache local (Caffeine ou ConcurrentHashMap) caso Redis esteja indisponível

### Prioridade Média (P1)
3. **Implementar Health Check** — adicionar `spring-boot-starter-actuator` e configurar `/actuator/health`
4. **Implementar Métricas** — expor métricas via Actuator + Micrometer
5. **Adicionar Flyway** — criar migrations versionadas para substituir `ddl-auto=update`

### Prioridade Baixa (P2)
6. **Implementar HATEOAS Links** — adicionar `spring-hateoas` e links de navegação nas respostas paginadas
7. **Adicionar role `USER`** — endpoints protegidos para usuários comuns
8. **Docker health check** — configurar `healthcheck` no `docker-compose.yml` ligado ao `/actuator/health`

---

## Resumo

| Item | Status |
|---|---|
| Health check | ❌ Pendente |
| Métricas | ❌ Pendente |
| Flyway migrations | ❌ Pendente |
| HATEOAS links | ❌ Pendente |
| Todos os 22 testes | ✅ Passando |
| Build Maven | ✅ Funcional |
