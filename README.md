# Fresh Keep — Backend

Spring Boot 4 (Java 21) backend for Fresh Keep.

## Product goal & problem it solves

Households lose track of what they've bought: receipts get thrown away, expiration dates on packaging are illegible or forgotten, and products end up discarded unopened because nobody remembers they're there. Fresh Keep lets a household ("space") photograph a shopping receipt and have AI extract each product and its storage spot and expiration date automatically, instead of entering everything by hand. From there, products can be moved between storage spots (fridge, freezer, pantry...), with the expiration date re-estimated by AI on each move, so the household always has an accurate view of what's in storage and what's close to expiring — the goal is fewer forgotten products and less food waste.

## Implemented features

- **Account & auth** — user/admin registration, login, JWT issuance and validation, role-based access control
- **Spaces & storage spots** — create a space, invite other users to it, create storage spots (fridge, freezer, pantry, etc.) within it
- **Shopping receipts** — upload a receipt image, AI-assisted extraction of the products on it (store, purchase date, products with name/type/expiration date/price), draft review and correction before confirming, re-processing a receipt if the AI's first extraction was wrong
- **Products** — manual product creation/update/deletion, moving a product between storage spots with AI-assisted expiration date recalculation, listing a space's products sorted by expiration date
- **Admin** — list registered users, browse shopping receipts and their products across the system, paginated

## Tech stack

- Spring Boot 4.0.7, Spring Data JPA, Spring Security, Bean Validation
- PostgreSQL, Flyway (`flyway-database-postgresql`) for schema migrations
- Spring AI 2.0.0 — Google GenAI (`gemini`) for shopping receipt extraction, Ollama (`llama3.1`) for lighter review/expiration-date tasks
- `io.jsonwebtoken` (jjwt) for JWT issuance
- springdoc-openapi for API docs
- Testcontainers (real Postgres in tests), JUnit 5, Mockito

## Architecture

Modular monolith, organized by feature module rather than by technical layer: `account`, `user`, `admin`, `space`, `product`, `shopping_receipt`, plus a `shared` module for cross-cutting code (base exception hierarchy, `GlobalExceptionHandler`).

Each module internally follows hexagonal architecture (ports & adapters), split into three layers:

- **`domain`** — framework-free business logic: aggregates/entities (`model/`), value objects (`value_object/`), domain exceptions (`exception/`). Entities validate their own invariants and expose named factory methods instead of public constructors/setters.
- **`application`** — use cases that orchestrate the domain, expressed as ports: `port/in` (inbound use-case interfaces + their request/response DTOs), `port/out` (outbound interfaces the application depends on but doesn't implement), `service` (use-case implementations, `@Service` + `@Transactional`), `command` (input commands built by controllers).
- **`infrastructure`** — adapters implementing `port/out` and exposing HTTP endpoints: `persistence/jpa` (Spring Data JPA entities/repositories/mappers), `security`, `web` (`@Controller`s and their own DTOs), `ai` (Spring AI adapters).

Modules never depend on each other's domain objects directly — cross-module references use typed ID value objects, and cross-module side effects (e.g. provisioning a `User` off an `Account` registration) go through domain events (`ApplicationEventPublisher` + `@TransactionalEventListener`) rather than direct calls, so modules stay decoupled while still committing atomically in the same transaction.

See [`CLAUDE.md`](CLAUDE.md) for the full set of architectural conventions this codebase follows.

## Authentication & roles

Authentication is stateless, via JWT bearer tokens. Two roles exist: `USER` (regular household members) and `ADMIN` (back-office access to users/receipts across spaces).

- `POST /api/v1/auth/register/user` — public, creates an `Account` + `User`
- `POST /api/v1/auth/register/admin` — `ADMIN`-only, creates an `Account` + `Admin`
- `POST /api/v1/auth/login` — public, returns `{ accountId, email, jwtString, expiresIn }`

Registration does **not** return a token — call `login` afterwards to obtain one. The first admin account is provisioned automatically at startup from the optional `ADMIN_BOOTSTRAP_EMAIL`/`ADMIN_BOOTSTRAP_PASSWORD` environment variables (needed to be able to call `register/admin` at all, since that endpoint itself requires an admin caller).

Every endpoint other than `/api/v1/auth/**` requires a valid JWT in the `Authorization: Bearer <token>` header, and most are additionally role-gated per-endpoint with `@PreAuthorize`. The JWT's claims carry `sub` (email), `roles`, `accountId`, and `userId`/`adminId` (whichever applies).

The full request/response contract, including error cases per endpoint, is documented in [`agents/api_contract.md`](agents/api_contract.md).

## Running locally

### Prerequisites

- Docker (for Postgres, Testcontainers, and running via Compose)
- A local [Ollama](https://ollama.com) instance with the `llama3.1` model pulled, if you want AI-assisted expiration date recalculation to work
- A Google GenAI API key, if you want shopping receipt processing to work

### Environment variables

Both Docker Compose files read from a single root `.env` file. Create one with:

```
DEV_POSTGRES_DATABASE=...
DEV_POSTGRES_USER=...
DEV_POSTGRES_PASSWORD=...
DEV_POSTGRES_PORT=...
DEV_APP_PORT=...

GOOGLE_GENAI_API_KEY=...
JWT_SECRET=...
JWT_EXPIRATION=...
CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...

# Optional — provisions the first admin account at startup
ADMIN_BOOTSTRAP_EMAIL=...
ADMIN_BOOTSTRAP_PASSWORD=...

# Optional — comma-separated, empty = no cross-origin access
CORS_ALLOWED_ORIGINS=http://localhost:5050
```

(`docker-compose.prod.yml` additionally expects `PROD_POSTGRES_*`/`PROD_APP_PORT` equivalents.)

### Via Docker Compose (recommended)

```bash
docker compose -f docker-compose.dev.yml up    # Postgres + app, dev profile
docker compose -f docker-compose.prod.yml up   # Postgres + app, prod profile
```

The app container reaches Ollama on the host via `host.docker.internal:11434`, so Ollama itself needs to be running on the host, not in a container.

### Via Maven (without Docker Compose)

```bash
./mvnw clean compile                   # compile
./mvnw spring-boot:run                 # run locally
./mvnw clean package                   # build the jar (target/fresh-keep-*.jar)
```

Running this way requires a `dev` or `prod` Spring profile and all the environment variables referenced in `application.properties` to be set manually — Docker Compose supplies these for you.

## Database migrations

Schema is managed by Flyway. Migrations live in `src/main/resources/db/migration` (`V{n}__description.sql`) and run automatically on application startup (`spring.flyway.enabled=true`) — there's no separate command to invoke. `spring.jpa.hibernate.ddl-auto=validate` in both profiles, so Hibernate never auto-generates DDL; any schema change must go through a new numbered migration file.

## API documentation (Swagger/OpenAPI)

With the app running locally: Swagger UI at `http://localhost:8080/swagger-ui.html`, raw OpenAPI spec at `http://localhost:8080/v3/api-docs`. There is no deployed environment yet, so no hosted Swagger UI link exists for now.

**Authenticating in Swagger UI** to try protected endpoints:
1. Open the `Auth` section, use `POST /api/v1/auth/register/user` (or `login`, if you already have an account) via "Try it out".
2. Copy the `jwtString` value from the response.
3. Click the **Authorize** button (top right, padlock icon), paste the raw token (no `Bearer ` prefix — Swagger UI adds that itself) into the `bearerAuth` field, and confirm.
4. Every subsequent "Try it out" call now sends that token automatically.

## Tests

```bash
./mvnw test                            # run all tests (excludes the llm-eval group by default)
./mvnw test -Dtest=ClassName                  # run a single test class
./mvnw test -Dtest=ClassName#methodName       # run a single test method
```

Tests use Testcontainers to spin up a real Postgres instance, so Docker must be running even when invoking Maven directly. Suites, by convention:

- **Unit tests** (`*Test`) — most of the suite; domain entities, use-case services (with ports mocked via Mockito), value objects.
- **Persistence tests** (`*RepositoryAdapterTest`, `@DataJpaTest`) — JPA mapping/round-trip correctness against a real (Testcontainers) Postgres.
- **Dedicated integration tests** (`*IntegrationTest`) — full `@SpringBootTest` + `@Testcontainers`, unrelated beans stubbed via `@MockitoBean`, used where transactional/event behavior (e.g. `@TransactionalEventListener` cross-module provisioning) needs to be exercised against the real Spring context rather than mocked.
- **`FreshKeepIntegrationTests`** — the single end-to-end HTTP test class (`@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@AutoConfigureMockMvc`), grouping coverage per API area in `@Nested` classes. New endpoint coverage is added as a nested class here rather than a standalone test class.
- **`llm-eval`-tagged tests** — opt-in evaluation tests that make real calls to the AI models (not run by default; excluded via the `excludedGroups` Maven property). Run them explicitly with `./mvnw test -DexcludedGroups=`.

## Git workflow

Work is tracked as GitHub Issues against the [User Stories board](https://github.com/users/isalvama/projects/4/views/1). Each issue is implemented on its own branch off `main`, named by type: `feat/<short-description>`, `fix/<short-description>`, `refactor/<short-description>`, `docs/<short-description>`, `config/<short-description>`. Work is submitted as a pull request into `main`; once reviewed, it's merged and the branch is deleted.

## Deployment

Not deployed yet — no live frontend or backend URL to link to at this time.
