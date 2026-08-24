# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Fresh Keep is a Spring Boot 4 (Java 21) backend. It is a modular monolith organized by feature module, with each module internally structured as a hexagonal architecture (ports & adapters). Implemented modules currently include `account` (registration, login, JWT issuance/validation — token persistence/revocation is not yet implemented), `user` (domain profile provisioned automatically off account registration, see below), and `space` (spaces/storage spots). `product` and `shopping_receipt` currently only have ID value objects defined, no aggregate/use-case layer yet.

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

Integration/end-to-end HTTP tests follow a single-class convention: `FreshKeepIntegrationTests` (`@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@Testcontainers` + `@AutoConfigureMockMvc`) groups tests per API area in `@Nested` classes (e.g. `Authentication` for `/api/v1/auth`), with `@BeforeEach` clearing relevant repositories (e.g. `accountSpringDataRepository.deleteAll()`) for isolation — every nested test class needs this, not just some of them, or leftover rows from one test break a later one. Add new endpoint coverage as a nested class here rather than a standalone test class.

For testing transactional/event behavior directly against a real Spring context and Postgres (rather than through HTTP), the convention is a dedicated `*IntegrationTest` class per service (e.g. `RegisterUserAccountServiceIntegrationTest`, `IdentityResolverServiceIntegrationTest`): full `@SpringBootTest` + `@Testcontainers`, with unrelated beans stubbed out via `@MockitoBean` (not mocked at the unit level) so the real wiring under test — repositories, event listeners, transaction boundaries — is exercised as-is.

Database schema is managed by Flyway; migrations live in `src/main/resources/db/migration` (`V{n}__description.sql`) and run automatically on startup (`spring.flyway.enabled=true`). `spring.jpa.hibernate.ddl-auto=validate` in both dev and prod — schema changes must go through a new Flyway migration, never through Hibernate auto-DDL.

## Architecture

### Module layout

Each feature lives under `modules/<name>` (e.g. `modules/account`, `modules/user`, `modules/space`) with three layers:

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

`shared/` holds cross-module code: `shared/domain` (the `Role` enum and the base exception hierarchy) and `shared/infrastructure` (`GlobalExceptionHandler`, mapping the hierarchy below to RFC7807 `ProblemDetail` responses, plus base infra exceptions). There is no longer a `shared/config` package — Spring Security `@Configuration` beans (`PasswordEncoder`, `UserDetailsService`, `AuthenticationManager`, the `SecurityFilterChain`) now live in `modules/account/infrastructure/security/config/` (`SpringSecurityConfiguration`, `AppSecurityConfiguration`), since they're currently account-specific.

The base exception hierarchy is `FreshKeepException` → `DomainException`, which branches into `ConflictException` → `AccountAlreadyExistsException`, `ForbiddenException` → `DisabledAccountException`, `UnauthorizedException` → `InvalidCredentialsException`, and validation exceptions (`InvalidAccountException`, `InvalidEmailException`, `InvalidTokenException`, etc.). Separately, `InfrastructureException` (`shared/infrastructure`) branches into exceptions representing broken system invariants rather than caller-facing business rules — `IdentityMappingException` (`account/infrastructure/security/exception`), `AdminProvisioningPendingException` (`account/infrastructure/security/exception`), `InvalidResolvedEntitiesException` (`account/infrastructure/security/exception`), and `UserProvisioningPendingException` (`user/infrastructure/user_id_look_up/exception`) — all mapped to `500` by `GlobalExceptionHandler`. New modules should follow the same `domain`/`application`/`infrastructure` split and reuse this exception hierarchy rather than throwing raw `RuntimeException`.

### Notable in-progress state

- Security is fully wired: `AppSecurityConfiguration` (`modules/account/infrastructure/security/config/`) defines a stateless `SecurityFilterChain` — `/api/v1/auth/register/admin` requires `ROLE_ADMIN`, the rest of `/api/v1/auth/**` is public, everything else requires authentication. `JwtAuthenticationFilter` validates the `Authorization: Bearer` header on each request and populates the `SecurityContextHolder` via `CustomUserPrincipal`. Spring Security matches `requestMatchers` in declaration order, so the admin-only rule must be declared before the broader `/api/v1/auth/**` permitAll rule — this ordering bug has recurred and been re-fixed more than once now; the endpoint stayed protected in between only because of the redundant `@PreAuthorize("hasRole('ADMIN')")` on `AuthController.registerAdmin` (defense in depth). Keep both layers in place, and keep the matcher order correct, when touching this config.
- Three endpoints exist under `/api/v1/auth` (`AuthController`): `POST register/user` (public), `POST register/admin` (`ROLE_ADMIN`-only, also `@PreAuthorize`-guarded), `POST login` (public, via `LoginUseCase`/`LoginService`). The two registration endpoints return `AuthRegisterResponse` (`accountId`, `email` only — **no token**); only `POST login` returns `AuthJwtResponse` (`accountId`, `email`, `jwtString`, `expiresIn`). A client must call `login` as a separate step after registering to obtain a session — this is deliberate (see next bullet for why).
- Cross-module identity resolution: `Account` (auth identity) and `User` (domain identity, `modules/user`) are separate aggregates with independently-generated IDs, linked via `User.accountId`. `User` creation is triggered by `UserAccountRegisteredEvent`, handled by `UserEventModuleListener` via `@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)` — deliberately *not* `@Async`, so `User` creation is atomic with `Account` creation (same transaction; if one fails, both roll back). Because of this timing, `LoginService` — not the registration services — is where `AccountId` gets resolved into `userId`/`adminId` for the JWT, via `IdentityResolverService` (`account/application/service`), which calls `UserIdentityLookUpPort`/`AdminIdentityLookUpPort` (`account/application/port/out`) based on the account's roles. Resolving these synchronously inside the registration transaction doesn't work — the event listener hasn't run yet at that point in the method body — which is why registration doesn't attempt it and returns no token. `AdminIdentityLookUpPort`'s current implementation (`AdminIdentityLookUpAdapter`) is a placeholder that always throws `AdminProvisioningPendingException` — **there is no real admin-provisioning module yet, so login for any `ADMIN`-role account currently always fails with 500.**
- `TokenRepositoryPort` still has no adapter implementation — token *persistence/revocation* is not wired up, but token *validation* on incoming requests works via `JwtAuthenticationFilter`/`JwtTokenGeneratorAdapter.isTokenValid()`. Don't assume tokens are persisted or revocable.
- 401/403 responses are returned as RFC7807 `ProblemDetail` JSON via `CustomAuthenticationEntryPoint` (401) and `CustomAccessDeniedHandler` (403), both in `infrastructure/security/handler/`.
- The `AuthController` endpoints' request/response contract, including the JWT's claim shape (`sub`, `roles`, `accountId`, `userId`, `adminId`), is documented for frontend consumers in `agents/api_contract.md` — keep it in sync when changing `AuthController`, `JwtTokenGeneratorAdapter`, or the response DTOs.

## Dependencies of note

- Spring Boot 4.0.7, Spring Data JPA, Flyway (`flyway-database-postgresql`), Spring Security, Bean Validation, springdoc-openapi (`springdoc-openapi-starter-webmvc-ui:3.0.2`).
- `spring-ai-starter-model-google-genai` (spring-ai BOM `2.0.0`) for Google GenAI integration (API key via `SPRING_AI_GOOGLE_GENAI_API_KEY`).
- `io.jsonwebtoken` (jjwt) for JWT issuance, configured via `jwt.secret` / `jwt.expiration`.
- Lombok is used throughout domain-adjacent infra classes (`@Data`, `@Builder`, `@RequiredArgsConstructor`) but deliberately avoided in domain entities, which hand-write validation and factory methods instead.