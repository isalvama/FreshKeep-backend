# AGENTS.md

Spring Boot 4 (Java 21) backend. Modular monolith — each feature module is internally hexagonal (ports & adapters). Build with the Maven wrapper `./mvnw`, not a system `mvn`.

## Commands

```bash
./mvnw test                          # all tests (needs Docker: Testcontainers Postgres)
./mvnw test -Dtest=ClassName         # single test class
./mvnw test -Dtest=ClassName#method  # single test method
./mvnw clean compile                  # compile — no lint/format tooling is configured
./mvnw spring-boot:run                # run locally (needs env vars below + a profile, dev|prod)
```

No checkstyle/spotless/error-prone is configured; `./mvnw clean compile` is the typecheck. Lombok runs as an annotation processor via `annotationProcessorPaths` in pom.xml — don't remove that when editing the build.

## Running the app

Via Docker Compose (Postgres + app), not bare `java -jar`:

```bash
docker compose -f docker-compose.dev.yml up
docker compose -f docker-compose.prod.yml up
```

Secrets live in `.env` (gitignored; `.env.example` is the allow-listed place to document required vars). Required env vars: `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`, `GOOGLE_GENAI_API_KEY` (Gemini), `CLOUDINARY_CLOUD_NAME/API_KEY/API_SECRET`, `JWT_SECRET`, `JWT_EXPIRATION`, plus compose-specific `DEV_POSTGRES_*` / `PROD_POSTGRES_*` and port vars.

- The GenAI env var is `GOOGLE_GENAI_API_KEY`; compose maps it to `SPRING_AI_GOOGLE_GENAI_API_KEY` for Spring's relaxed binding. Either spelling resolves to `spring.ai.google.genai.api-key`.
- The dev compose stack expects a local Ollama daemon on `localhost:11434` (via `host.docker.internal`) with `llama3.1:latest` pulled — receipt processing/review endpoints fail without it.
- Both profiles set `spring.jpa.hibernate.ddl-auto=validate`; schema changes go through a new Flyway migration in `src/main/resources/db/migration` (`V{n}__description.sql`), never Hibernate auto-DDL.

## Tests

- **All integration/adapter tests need Docker** — Testcontainers spins up real Postgres (`postgres:16-alpine`); no H2/test doubles for persistence.
- **HTTP endpoint coverage goes in `FreshKeepIntegrationTests`** (single class, `@SpringBootTest(RANDOM_PORT)` + `@Testcontainers` + `@AutoConfigureMockMvc`), one `@Nested` class per API area. Every nested class needs its own `@BeforeEach` cleanup (`repository.deleteAll()` / `jdbcTemplate` deletes) — leftover rows from one nested class break later ones. This is enforced by convention, not tooling.
- **Transactional/event behavior per service** gets a dedicated `*IntegrationTest` class (full `@SpringBootTest` + `@Testcontainers`), stubbing unrelated beans with `@MockitoBean`.
- **`llm-eval` tests are excluded by default** (surefire `excludedGroups` in pom.xml). Two nested classes in `FreshKeepIntegrationTests` exercise real Gemini + Ollama + Cloudinary end-to-end. To run one, you need all of: real credentials exported, local Ollama reachable on `localhost:11434`, receipt photos dropped into `src/test/resources/receipts` (gitignored — never commit them), and the group exclusion cleared:

  ```bash
  GOOGLE_GENAI_API_KEY=... CLOUDINARY_CLOUD_NAME=... CLOUDINARY_API_KEY=... CLOUDINARY_API_SECRET=... \
    ./mvnw test -Dtest='FreshKeepIntegrationTests$ProcessNewShoppingReceipt' -DexcludedGroups=
  ```

  Quote the `$` in nested-class names. Missing prerequisites make these skip (JUnit `Assumptions`), not fail.
- `TestFreshKeepApplication` boots the app locally against the Testcontainers Postgres — use it for manual poking without a real DB.

## Architecture

Modules under `src/main/java/com/isalvama/fresh_keep/modules/`: `account` (auth identity, JWT, security config), `user`, `admin` (admin dashboards/metrics), `space` (spaces, storage spots, invitations), `product`, `shopping_receipt`. Each module: `domain` (framework-free), `application` (`port/in` use cases + `command`s, `port/out`, `service`), `infrastructure` (`persistence/jpa`, `web`, `event`, per-topic `*_look_up` adapters).

Rules that recur in review:
- Domain entities: **no Lombok**, private constructors + named factories (`Account.createUser()`, `Account.reconstitute()`), self-validating invariants. Value objects are validating records. Lombok is fine in infrastructure/application-layer classes.
- Services implement `port/in` interfaces and depend **only on `port/out` interfaces**, never on infrastructure classes. Cross-module reads follow the lookup-port pattern: the *consuming* module declares a `*LookUpPort` in its `port/out`; the *owning* module implements it under `infrastructure/<name>_look_up/` by delegating to its own use cases (e.g. `shopping_receipt`'s `SpaceLookUpPort` → `space`'s `SpaceLookUpAdapter`).
- Exceptions: `FreshKeepException` → `DomainException` (conflict/forbidden/unauthorized/validation — caller-facing) vs `InfrastructureException` (broken system invariants → 500). Reuse this hierarchy; no raw `RuntimeException`. `GlobalExceptionHandler` (`shared/infrastructure/web/handler`) maps it all to RFC7807 `ProblemDetail`, including 401/403 bodies.
- `shared/` holds `Role`, `Email`, the exception hierarchy, `GlobalExceptionHandler`, the AI adapters, Cloudinary image storage, and `shared/config/AppConfig` (`@EnableRetry` + `Clock` bean — inject `Clock`, never `Instant.now()`/`LocalDate.now()` directly in logic under test).
- AI: Gemini (`gemini-3.6-flash`) does receipt extraction (`GenAiShoppingReceiptProcessorAdapter`); Ollama (`llama3.1:latest`) does extraction review and product-moved expiration-date recalculation. AI calls are `@Retryable(AiRetryableException)`; SDK failures are translated by `GenAiExceptionTranslator` into `AiRateLimited/AiRetryable/AiUnprocessableInput`.
- `agents/api_contract.md` documents the Auth/Space (incl. invitations)/Shopping Receipt/Product/Admin HTTP contracts (incl. JWT claim shape `sub/roles/accountId/userId/adminId`) for the frontend — keep it in sync when changing controllers, response DTOs, or the JWT.

## Auth/security gotchas (recurred bugs)

- `AppSecurityConfiguration` (`modules/account/infrastructure/security/config`) is stateless: `/api/v1/auth/register/admin` requires `ROLE_ADMIN`, rest of `/api/v1/auth/**` is public, everything else authenticated. **Matcher order matters** — the admin rule must be declared before the `/api/v1/auth/**` permitAll; this ordering bug has been re-fixed more than once. Keep the redundant `@PreAuthorize("hasRole('ADMIN')")` on `AuthController.registerAdmin` and on all `AdminController` endpoints as defense in depth.
- Registration endpoints return **no token** (deliberate): `User`/`Admin` aggregates are provisioned by `@TransactionalEventListener(phase = BEFORE_COMMIT)` — *not* `@Async` — so provisioning is atomic with `Account` creation, but identity (`userId`/`adminId`) can't be resolved inside the registration transaction (the listener hasn't run yet). `LoginService` resolves identity via `IdentityResolverService`. A client must call `login` after registering.
- **ADMIN login currently always fails with 500**: `AdminIdentityLookUpAdapter` (`modules/account/infrastructure/AdminIdentityLookUpAdapter.java`) is a placeholder that throws `AdminProvisioningPendingException`. Don't build admin login flows/tests against it.
- `TokenRepositoryPort` has no adapter — tokens are neither persisted nor revocable, but per-request validation works (`JwtAuthenticationFilter`).
