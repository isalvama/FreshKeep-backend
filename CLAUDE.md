# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Fresh Keep is a Spring Boot 4 (Java 21) backend. It is a modular monolith organized by feature module, with each module internally structured as a hexagonal architecture (ports & adapters). Currently the only implemented module is `account`, which covers user registration and JWT issuance; login and token persistence/revocation are not yet implemented.

## Common commands

Build and run via the Maven wrapper (`./mvnw`), not a system-installed `mvn`.

```bash
./mvnw clean compile          # compile
./mvnw test                   # run all tests
./mvnw test -Dtest=ClassName                  # run a single test class
./mvnw test -Dtest=ClassName#methodName       # run a single test method
./mvnw clean package           # build the jar (target/fresh-keep-*.jar)
./mvnw spring-boot:run          # run the app locally
```

Running the app requires a Spring profile (`dev` or `prod`) and the environment variables referenced in `application.properties`: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_AI_GOOGLE_GENAI_API_KEY`, `JWT_SECRET`, `JWT_EXPIRATION`. These are normally supplied via `.env` and the Docker Compose files rather than exported manually:

```bash
docker compose -f docker-compose.dev.yml up    # Postgres + app, dev profile
docker compose -f docker-compose.prod.yml up   # Postgres + app, prod profile
```

Tests use Testcontainers (`TestcontainersConfiguration`) to spin up a real Postgres instance, so Docker must be available when running the test suite. `TestFreshKeepApplication` boots the app locally wired to the Testcontainers Postgres instance (useful for manual testing without a real DB).

Database schema is managed by Flyway; migrations live in `src/main/resources/db/migration` (`V{n}__description.sql`) and run automatically on startup (`spring.flyway.enabled=true`). `spring.jpa.hibernate.ddl-auto=validate` in both dev and prod — schema changes must go through a new Flyway migration, never through Hibernate auto-DDL.

## Architecture

### Module layout

Each feature lives under `modules/<name>` (currently only `modules/account`) with three layers:

- **`domain`** — framework-free business logic: aggregate/entity classes (`model/`), value objects (`value_object/`), and domain-specific exceptions (`exception/`). Entities validate their own invariants in private constructors and expose named factory methods instead of public constructors/setters (e.g. `Account.createUser()`, `Account.createAdmin()`, `Account.reconstitute()`). Value objects are validating Java records (`Email`, `AccountId`, `AuthToken`).
- **`application`** — use cases that orchestrate the domain, expressed as ports:
  - `port/in` — inbound use-case interfaces (e.g. `AuthenticationUseCase`) plus their request/response DTOs (`port/in/dto/request`, `port/in/dto/response`).
  - `port/out` — outbound interfaces the application depends on but does not implement (`AccountRepositoryPort`, `TokenRepositoryPort`, `PasswordHasherPort`, `JwtTokenGeneratorPort`).
  - `service` — use-case implementations (`@Service`, `@Transactional`) that implement the `port/in` interfaces and depend only on `port/out` interfaces, never on infrastructure classes directly.
  - `command` — input commands built by controllers and passed into use cases (e.g. `RegisterAccountCommand`), distinct from the inbound DTOs.
- **`infrastructure`** — adapters that implement `port/out` interfaces and expose HTTP endpoints:
  - `persistence/jpa/<aggregate>` — Spring Data JPA entities (`Jpa*Entity`), Spring Data repositories, a `*RepositoryAdapter` implementing the corresponding `port/out` interface, and a `mapper/` component converting between JPA entities and domain objects. Domain objects never carry JPA annotations.
  - `security` — JWT generation (`JwtTokenGeneratorAdapter` using `io.jsonwebtoken`), password hashing (`SpringSecurityPasswordHasher`), and a `UserDetails` adapter (`CustomUserPrincipal`) that decouples Spring Security from the domain model.
  - `web` — `@Controller` REST endpoints and their own DTOs (`web/dto`), separate from the `application/port/in/dto` DTOs used by the use case layer.

`shared/` holds cross-module code: `shared/config` (Spring `@Configuration` beans, e.g. `AppConfig`'s `PasswordEncoder`) and `shared/domain` (the `Role` enum and the base exception hierarchy `FreshKeepException` → `DomainException` → `ConflictException` → module-specific exceptions like `AccountAlreadyExistsException`). New modules should follow the same `domain`/`application`/`infrastructure` split and reuse this exception hierarchy rather than throwing raw `RuntimeException`.

### Notable in-progress state

- `TokenRepositoryPort` has no adapter implementation yet — token persistence/revocation was intentionally removed (see git history) and is not wired up. Don't assume tokens are persisted.
- There is no Spring Security `SecurityFilterChain` configured yet, so JWTs are issued on register but not yet validated on subsequent requests.
- Only registration (`POST /auth/user/register`) exists; there is no login endpoint yet.

## Dependencies of note

- Spring Boot 4.0.7, Spring Data JPA, Flyway (`flyway-database-postgresql`), Spring Security, Bean Validation, springdoc-openapi.
- `spring-ai-starter-model-google-genai` for Google GenAI integration (API key via `SPRING_AI_GOOGLE_GENAI_API_KEY`).
- `io.jsonwebtoken` (jjwt) for JWT issuance, configured via `jwt.secret` / `jwt.expiration`.
- Lombok is used throughout domain-adjacent infra classes (`@Data`, `@Builder`, `@RequiredArgsConstructor`) but deliberately avoided in domain entities, which hand-write validation and factory methods instead.