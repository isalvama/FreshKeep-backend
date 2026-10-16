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

### Domain model design (DDD)

The `domain` layer of each module follows tactical Domain-Driven Design. This section explains the rules applied and how each aggregate is shaped.

#### Building blocks

- **Aggregate.** A cluster of objects treated as one consistency unit. It is loaded, validated and saved as a whole through its **aggregate root**, which is the only object outside code may hold or modify. Each aggregate has exactly one repository port (`*RepositoryPort`), and there is no repository for anything *inside* an aggregate.
- **Entity.** An object with an identity that persists across changes (a `Product` is still the same product after it's moved or renamed). Entities here are classes with a typed ID field.
- **Value object.** An object defined only by its value, with no identity: immutable, compared by value, and valid by construction. Here they're Java `record`s that validate in their compact constructor (`Email`, `Money`, `Amount`, `SpaceName`, `Count`, every `*Id`...).

#### Aggregates in the codebase

| Module | Aggregate root | Contains (internal entities / value objects) | References other aggregates by ID |
|---|---|---|---|
| `account` | `Account` | `Email`, `Set<Role>` | — |
| `user` | `User` | `Email`, `UserName` | `AccountId` |
| `admin` | `Admin` | `Email` | `AccountId` |
| `space` | `Space` | **`Set<StorageSpot>`** (entities), `SpaceName`, `Emoji` | `UserId` (creator), `Set<UserId>` (participants) |
| `space` | `SpaceInvitation` | `Token`, `Count` (uses/max uses) | `SpaceId`, `UserId` (creator) |
| `shopping_receipt` | `ShoppingReceipt` | `ShoppingReceiptStatus` | `UserId` (creator), `SpaceId`, `ReceiptImageId` |
| `shopping_receipt` | `ReceiptImage` | `AssetId` (external image storage) | — |
| `product` | `Product` | `ProductName`, `Money` (`Amount` + `Currency`), `ProductType` | `ShoppingReceiptId`, `StorageSpotId` (suggested & actual) |

#### Rule 1: aggregates reference each other by ID only

No aggregate holds an object reference to another aggregate. `Product` has a `StorageSpotId`, not a `StorageSpot`. `ShoppingReceipt` has a `SpaceId` and a `UserId`, not a `Space` and a `User`. `Space.participantIds` is a `Set<UserId>`, not a `Set<User>`. The reasons:

- **Clear consistency boundaries.** If `Product` held a `StorageSpot` object, saving a product could implicitly change a storage spot, and the `Space` invariants that protect storage spots (see below) would be bypassed. With an ID, the only way to change a storage spot is through `Space`.
- **Small, cheap loads.** Loading a product doesn't drag in its receipt, space, participants and storage spots. Each use case loads only the aggregates it actually modifies.
- **Module isolation.** Most of these references cross module boundaries (`product` → `space`, `shopping_receipt` → `user`). Sharing only an ID value object means no module depends on another's aggregate classes, and the referenced module can change its model freely. When a use case needs *data* behind an ID, it asks through a [look-up port](#2-look-up-ports-asking-another-module-for-information) instead of navigating an object graph.
- **Typed IDs, not raw `UUID`s.** Each ID is its own record (`ProductId`, `StorageSpotId`, `UserId`...). Passing a `SpaceId` where a `StorageSpotId` is expected won't compile. Parsing (`StorageSpotId.from(String)`) validates null/blank/UUID format and throws `InvalidIdException`, so a malformed ID never reaches the domain.

A consequence is that **an ID reference doesn't guarantee the referenced aggregate exists or is accessible**. That's checked by application services before acting. For example, `MoveProductService` verifies through `SpaceParticipancyLookUpPort` that the user participates in the spaces of both storage spots, and `ConfirmShoppingReceiptService` checks that the receipt's creator, space and image match the request.

#### Rule 2: what goes inside an aggregate vs. what becomes its own aggregate

The question asked for each relationship is: *is there an invariant that needs both objects to be checked together, in the same transaction?* If yes, they belong to one aggregate. If not, they're separate aggregates linked by ID.

**`Space` contains its `StorageSpot`s.** This is the codebase's main example of an aggregate with internal entities:

- `StorageSpot` is an entity: it has its own `StorageSpotId`, because products need to point at it and it can be renamed. But it has no meaning outside its space, and there's an invariant spanning the whole set: *a space must have at least one storage spot, and no two of its storage spots may share the same name and type*. Only something that sees the whole set can enforce that, so `Space`'s constructor validates it (`validateNotNullNotEmptyNotDuplicateStorageSpots`) and throws `InvalidSpaceException` otherwise.
- So `StorageSpot` has no repository port of its own. Storage spots are created with their space (`Space.create(name, emoji, storageSpots, creatorId)`), persisted through `SpaceRepositoryPort`, and reconstituted together with it.
- Other modules can still *refer* to a storage spot by its `StorageSpotId`. Being inside an aggregate means nobody can modify the spot except through its root, not that it can't be referenced.

**`SpaceInvitation` is not inside `Space`.** It belongs to a space, but none of its rules involve the space's other data. Its lifecycle (expiry, use count, active flag) is independent. A space may have many invitations over time, and joining by invitation would otherwise need to load and lock the whole `Space`. So it's a separate aggregate referencing `SpaceId`.

**`Product` is not inside `ShoppingReceipt`.** A receipt "has" products conceptually, but:

- Products live on long after their receipt is confirmed. They're moved between storage spots, edited, deleted and listed per space, and none of that involves the receipt.
- No invariant spans the receipt and its products. Each product's rules (expiration date handling, price consistency) concern only that product.
- Putting them together would mean loading and saving the entire receipt (and every product on it) to move one yogurt to the freezer. It would also turn concurrent edits of *different* products by household members into conflicts on the same aggregate.

So `Product` is its own aggregate in its own module, referencing `ShoppingReceiptId`. The receipt only creates them once, through `ProductRegistrationPort` on confirmation.

**`ReceiptImage` is not inside `ShoppingReceipt`** either. It's created first, when the image is uploaded and stored externally, before any receipt exists. It's immutable, and it's referenced from the receipt by `ReceiptImageId`.

**`Account`, `User` and `Admin` are three aggregates**, not one. Authentication identity (email, password hash, roles) changes for different reasons than the domain profile, and lives in a different module. They're linked by `AccountId` and kept consistent through [domain events](#1-domain-events-reacting-to-something-another-module-did).

#### Rule 3: always-valid aggregates, built through named factories

Every entity has a **private constructor** that validates its invariants (non-null fields, the storage spot rules above, non-empty roles...). It's only reachable through two kinds of static factory:

- **`create(...)` / `createUser(...)` / `createDraft(...)`** — for *new* instances. It generates the ID (`XxxId.create()`), sets the initial state, and applies **creation-only rules**:
  - `ShoppingReceipt.createDraft` always starts in `DRAFT` and rejects a purchase date in the future.
  - `Space.create` makes the creator the only participant.
  - `Product.create` sets `actualStorageSpotId` to the suggested one.
  - `Account.createUser`/`createAdmin` assign the matching role.
  - `SpaceInvitation.createWithoutMaxCount` generates a token, sets the 24h expiry and starts the use count at 0.
- **`reconstitute(...)`** — for rebuilding an existing instance from persistence (used by the JPA mappers). It takes the stored ID and state as-is and skips creation-only rules, since a receipt purchased last year is still valid today. It still goes through the constructor, so structural invariants are re-checked on every load. `SpaceInvitation.reconstitute` additionally checks that `usesCount` doesn't exceed `maxUses`.

As a result, any `Product`, `Space` or `ShoppingReceipt` instance in memory is valid. A service can't build a half-initialized one, and an invariant violation raises a domain exception (`InvalidProductException`, `InvalidSpaceException`...), mapped to a 4xx by `GlobalExceptionHandler`.

#### Rule 4: behaviour lives in the aggregate (no anemic model)

State changes are expressed as intention-revealing methods on the root that enforce the business rules, rather than setters orchestrated by a service:

- **`Product.updateStorageSpot(newStorageSpotId, newExpirationDate, clock)`** — moving a product changes its storage spot and adopts the AI-recalculated expiration date, with two rules:
  - an **already-expired product keeps its expiration date** (moving it to the freezer doesn't make it fresh again);
  - a recalculated date in the past is **clamped to today**.
  
  `updateStorageSpotWhenExpired` and `isExpired(clock)` support the same flow.
- **`Product.update(name, expirationDate, productType, amount, currency)`** — a partial update where `null` means "unchanged". Price changes keep `Money` consistent: amount and currency can be changed independently when a price exists, but both are required if the product had no price (`InvalidProductPriceUpdateException`). `suggestedStorageSpotId` and `shoppingReceiptId` are `final`: where the AI suggested storing a product, and which receipt it came from, are historical facts that never change.
- **`ShoppingReceipt.confirm(purchaseDate, storeName)`** — moves the receipt from `DRAFT` to `CONFIRMED` with the user-reviewed data.
- **`SpaceInvitation.use(clock)`** — rejects the invitation if it's expired, inactive, or has reached `maxUses` (`ExpiredSpaceInvitationException`). Otherwise it increments `usesCount`.
- **`Account.addRole(role)` / `hasRole(role)`** — rejects null or duplicate roles. This is how an existing `USER` account is promoted to `ADMIN` instead of a second account being created.

Time-dependent rules take a `java.time.Clock` parameter instead of calling `LocalDate.now()` directly. The domain stays deterministic, and tests can pin "today".

Rules that need data **outside** the aggregate stay in the application service. An aggregate can only guarantee what it can see. Examples:
- "the user must participate in the space owning this storage spot";
- "the user is already a participant of this space";
- "the confirmed receipt must belong to this creator, space and image".

The service gathers that data (via repository and look-up ports) and then calls the aggregate's method.

#### Rule 5: value objects for every concept with rules

Primitives are wrapped in value objects whenever the concept carries validation or behaviour. A raw `String`/`BigDecimal`/`Integer` never travels through the domain unchecked:

- **`Email`** trims and lowercases, enforces 8–30 characters and the RFC-style format (`InvalidEmailException`), so two spellings of the same address are equal.
- **`Amount`** rejects null/negative values and normalizes to 2 decimals (`HALF_UP`), so `9.9` and `9.90` are the same amount. **`Money`** pairs it with a `Currency` and can't exist without both.
- **`Count`** rejects negatives, and `add()` returns a *new* `Count` rather than mutating (immutability).
- **`Token`**, **`SpaceName`**, **`StorageSpotName`**, **`ProductName`**, **`UserName`**, **`Emoji`** each enforce their own format.
- Closed sets of values are enums with domain-aware parsing: `ProductType.getValueOf` throws `InvalidProductTypeException` instead of a raw `IllegalArgumentException`. The same pattern applies to `Currency`, `StorageSpotType`, `ShoppingReceiptStatus` and `Role`.

Because a value object is validated once, at construction, an entity that receives a `Money` or an `Email` doesn't need to re-check it.

#### Rule 6: the domain is persistence- and framework-ignorant

Domain classes carry no JPA or Spring annotations, only Lombok getters. Persistence is a separate model (`Jpa*Entity` in `infrastructure/persistence/jpa`), translated by mappers that call `reconstitute(...)` on the way in. The two models can therefore differ. For example:
- `Space.participantIds` is a plain `Set<UserId>`, while the JPA side has a dedicated participant entity with join timestamps.
- `Product` has no version field, while `JpaProductEntity` uses `@Version` for optimistic locking. The repository adapter translates a stale write into the domain's `ProductConcurrentlyModifiedException` (409).

#### Rule 7: one transaction, one use case, and cross-aggregate consistency is explicit

Each use case loads the aggregate(s) it changes through their repository ports, calls their domain methods, and saves them within the service's `@Transactional` boundary. When a single business operation spans aggregates, the coordination is explicit in the application layer, never hidden inside an aggregate:
- Registration: `Account` plus `User`/`Admin`, through a same-transaction event.
- Receipt confirmation: `ShoppingReceipt` plus new `Product`s, through a command port.
- Joining a space: `SpaceInvitation.use(...)` plus adding the participant to the `Space`.

Being a monolith on one database, the codebase deliberately chooses **transactional** rather than eventual consistency for these, so a failure in any step rolls the whole operation back.

### How modules communicate

Modules never call each other's services, repositories or aggregates directly. A module's application layer may reference another module's typed ID value objects (`UserId`, `SpaceId`, `StorageSpotId`, `AccountId`...) and, in a few places, its domain exceptions, but nothing else. Everything else that crosses a module boundary goes through one of three mechanisms, chosen by what the caller needs:

| Need | Mechanism | Direction of the call | Example |
|---|---|---|---|
| "Something happened, whoever cares should react". No answer needed | **Domain event** | Fire and forget, publisher doesn't know who listens | `account` → `user`/`admin` on registration |
| "I need some information you own" | **Look-up port** | Synchronous read, returns a value | `product` asks `space` whether a user can access a storage spot |
| "Do this task for me and tell me the result" | **Command port** | Synchronous write, returns a result | `shopping_receipt` asks `product` to register the confirmed products |

#### 1. Domain events: reacting to something another module did

Used when a module needs a side effect in another module but shouldn't know (or care) which module performs it. Today this is how `User` and `Admin` profiles get provisioned whenever an `Account` is registered.

How the pieces fit, using user registration as the example:

1. **The event is a plain record owned by the publisher**: `UserAccountRegisteredEvent(UUID accountId, String email)` in `account/application/port/out/dto/event/`. It only carries primitives/IDs, never the `Account` aggregate, so listeners can't reach into `account`'s domain model.
2. **The publisher is an outbound port**, so the application service stays framework-free: `RegisterUserAccountService` depends on `UserAccountRegisteredEventPublisher` (`account/application/port/out`) and calls `publish(UserAccountRegisteredEvent.from(account))` after saving the account.
3. **The infrastructure adapter bridges that port to Spring**: `AccountEventPublisherAdapter` (`account/infrastructure/event/`) implements both `UserAccountRegisteredEventPublisher` and `AdminAccountRegisteredEventPublisher` by delegating to Spring's `ApplicationEventPublisher.publishEvent(...)`.
4. **The listener lives in the consuming module's infrastructure layer** and turns the event into that module's own command, then calls its own inbound use case, exactly like a controller would:

   ```java
   // user/infrastructure/event/UserEventModuleListener.java
   @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
   public void handle(UserAccountRegisteredEvent event) {
       registerUserUseCase.execute(RegisterUserCommand.builder()
               .accountId(event.accountId().toString())
               .email(event.email())
               .build());
   }
   ```

   `AdminEventModuleListener` (`admin/infrastructure/event/`) does the same for `AdminAccountRegisteredEvent` → `RegisterAdminUseCase`.

**Timing and transactions.** These details matter for correctness:

- `publishEvent(...)` doesn't run the listener straight away. `@TransactionalEventListener` holds the event until the publisher's transaction reaches the phase it names. With `BEFORE_COMMIT`, the listener runs just before the registration transaction commits, **inside the same transaction**.
- So `Account` and `User` creation are **atomic**: if `RegisterUserUseCase` throws (for example, because the user already exists), the whole transaction rolls back and no orphan account is left behind. That's why the listeners are deliberately *not* `@Async` and don't use `AFTER_COMMIT`. Either of those would let the account commit while user creation failed.
- The listener only fires if the event is published **inside an active transaction** (the publishing service is `@Transactional`). Published outside one, it's silently dropped (Spring's default `fallbackExecution = false`).
- Because the listener runs at the *end* of the transaction, the `User` row **doesn't exist yet** anywhere in the publishing method's body. That's why registration can't resolve the `userId` and returns no JWT, and why `userId`/`adminId` are resolved at login instead, through look-up ports (see below).

The dependency arrow points from consumer to producer: `user` and `admin` import `account`'s event records, while `account` knows nothing about its listeners. A new module can react to registration just by adding its own listener, with no change to `account`.

#### 2. Look-up ports: asking another module for information

Used when a use case needs, synchronously, data owned by another module. It follows the hexagonal rule that **the module that needs something defines the interface, and the module that owns the data implements it**:

- **The port is declared by the consumer**, in its own `application/port/out`, in its own vocabulary and with its own DTOs (in `port/out/dto`). The consumer's services depend only on this interface. They don't know which module answers, or how.
- **The adapter lives in the provider module**, under a dedicated `infrastructure/<thing>_look_up/` package. It implements the consumer's port by delegating to the provider's *own* repository port or inbound use case, then maps the result into the consumer's DTOs. The provider's domain objects never leak out.

This is dependency inversion across modules: the provider's infrastructure depends on the consumer's application port, never the other way around, so the consumer's application layer stays free of any knowledge of the provider.

Example: identity resolution at login. `account` needs the `userId`/`adminId` linked to an account to put into the JWT, but `User` and `Admin` belong to other modules:

```java
// account/application/port/out/UserIdentityLookUpPort.java (owned by account, the consumer)
public interface UserIdentityLookUpPort {
    UUID getUserIdByAccountId(AccountId accountId);
}

// user/infrastructure/user_id_look_up/UserIdentityLookUpAdapter.java (owned by user, the provider)
@Component
public class UserIdentityLookUpAdapter implements UserIdentityLookUpPort {
    private final UserRepositoryPort userRepositoryPort;   // user's own outbound port

    public UUID getUserIdByAccountId(AccountId accountId) {
        return userRepositoryPort.findByAccountId(accountId.value())
                .map(u -> u.getId().value())
                .orElseThrow(() -> new UserProvisioningPendingException(...));
    }
}
```

`IdentityResolverService` (`account/application/service`) calls `UserIdentityLookUpPort` and/or `AdminIdentityLookUpPort` depending on the account's roles, and `LoginService` puts the result into the token. If no row is found, the adapter throws an `InfrastructureException` subtype (`UserProvisioningPendingException` / `AdminProvisioningPendingException`), mapped to `500`. Since events provision these rows atomically with the account, a missing row means a broken system invariant, not a caller error.

Current look-up ports:

| Port (consumer, `application/port/out`) | Adapter (provider, `infrastructure/`) | Answers |
|---|---|---|
| `account` · `UserIdentityLookUpPort` | `user` · `user_id_look_up/UserIdentityLookUpAdapter` | `userId` for an `AccountId` |
| `account` · `AdminIdentityLookUpPort` | `admin` · `admin_id_look_up/AdminIdentityLookUpAdapter` | `adminId` for an `AccountId` |
| `product` · `SpaceParticipancyLookUpPort` | `space` · `space_participancy_look_up/SpaceParticipancyLookUpAdapter` | whether a user participates in the space owning a storage spot; which of a set of spots they can access |
| `product` · `StorageSpotLookUpPort` | `space` · `storage_spot_look_up/StorageSpotLookUpAdapter` | name/type of storage spots by ID |
| `product` · `ProductShoppingDateLookUpPort` | `shopping_receipt` · `product_shopping_date_look_up/ProductShoppingDateLookUpAdapter` | the purchase date of the receipt a product came from (used for expiration date recalculation) |
| `shopping_receipt` · `SpaceLookUpPort` | `space` · `space_look_up/SpaceLookUpAdapter` | storage spots of a space the user participates in (used to suggest where to store extracted products) |
| `shopping_receipt` · `ProductCategoriesLookUpPort` | `product` · `product_categories_look_up/ProductCategoriesLookUpAdapter` | valid product types/currencies (fed to the AI prompt) and the preferred storage spot type per product type |
| `space` · `SpaceProductsLookUpPort` | `product` · `space_products_look_up/SpaceProductsLookUpAdapter` | the products stored in a space (for the space overview) |

Adapters delegate to whatever on the provider side fits the question:

- **The provider's repository port**, for plain reads: `UserIdentityLookUpAdapter`, `StorageSpotLookUpAdapter`, `SpaceParticipancyLookUpAdapter`, `ProductShoppingDateLookUpAdapter`.
- **The provider's inbound use case**, when the provider's own business rules (e.g. access checks) must apply: `SpaceLookUpAdapter` calls `GetStorageSpotsUseCase`, so `shopping_receipt` gets the same participancy check as an HTTP caller would.
- **The provider's domain knowledge**, with no persistence at all: `ProductCategoriesLookUpAdapter` exposes the `ProductType`/`Currency` enums and `product`'s product type → storage spot type mapping.

#### 3. Command ports: asking another module to do a task

Same shape as a look-up port (consumer-owned interface, provider-owned adapter), but the call *changes state* in the provider and returns the result. Unlike events, the caller needs the outcome right away.

When a shopping receipt is confirmed, `shopping_receipt` doesn't create `Product`s itself, because products belong to `product`. `ConfirmShoppingReceiptService` calls `ProductRegistrationPort.registerProducts(List<RegisterProductDto>)` (`shopping_receipt/application/port/out`). `ProductRegistrationAdapter` (`product/infrastructure/product_registration/`) maps those DTOs to a `RegisterProductsCommand` and runs `product`'s own `RegisterProductsUseCase`, so all of `product`'s validation and invariants apply. It then maps the `RegisteredProductResult`s back into `shopping_receipt`'s `RegisteredProductDto`s. Both services are `@Transactional`, so the product registration joins the confirmation's transaction and either both succeed or both roll back.

#### Read side: admin query ports

The `admin` back office needs cross-module, paginated, aggregated views (users with their spaces and receipts, receipts with their products, daily creation metrics). Rebuilding those from several look-up ports would mean N+1 calls and in-memory joins. Instead, `admin` defines read-only query ports (`UsersQueryPort`, `ShoppingReceiptQueryPort`, plus `product`'s `ProductQueryPort`), and their adapters (`admin/infrastructure/persistence/jpa/*_query/`) build JPA Criteria queries straight over other modules' JPA entities (`JpaUserEntity`, `JpaShoppingReceiptEntity`, `JpaProductEntity`) into flat projections/DTOs.

This is a deliberate, read-only exception to module isolation. It never loads or modifies another module's aggregates, so their invariants can't be bypassed, and all writes still go through the owning module's use cases.

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
