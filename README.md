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

#### Read side: query ports

Cross-module, read-only views (mainly for the admin back office) don't go through these three mechanisms. They use query ports that read other modules' tables directly. See [Query ports (read side)](#query-ports-read-side).

### AI adapters (Google GenAI & Ollama)

AI is just another outbound dependency. The application layer of `shopping_receipt` and `product` only knows about outbound ports defined in its own vocabulary. The adapters that implement them with Spring AI live together in `shared/infrastructure/ai/`, one sub-package per port:

| Port (consumer, `application/port/out`) | Adapter (`shared/infrastructure/ai/`) | Model | Used by | Task |
|---|---|---|---|---|
| `shopping_receipt` · `AiShoppingReceiptProcessorPort` | `shopping_receipt_processor/GenAiShoppingReceiptProcessorAdapter` | Google GenAI (`gemini-3.6-flash`, temperature `0.1`) | `ProcessNewShoppingReceiptService`, `ReProcessShoppingReceiptService` | Read the receipt **image** and extract store, purchase date and products (`process`), or re-examine only the products flagged as wrong (`reprocess`) |
| `shopping_receipt` · `AiReceiptExtractionReviewerPort` | `receipt_extraction_reviewer/OllamaReceiptExtractionReviewerAdapter` | Ollama (`llama3.1:latest`, local) | `ProcessNewShoppingReceiptService` | Review the extraction as **text** and flag implausible products (garbled name, wrong shelf life, wrong storage spot/type, odd price) |
| `product` · `ProductMovedExpirationDateCalculatorPort` | `product_moved_expiration_date_calculator/OllamaProductMovedExpirationDateCalculatorAdapter` | Ollama (`llama3.1:latest`, local) | `MoveProductService` | Recalculate a product's expiration date when it's moved to another storage spot, given its spot history |

**Why two providers.** Receipt extraction is the hard, multimodal task: it needs a vision model that can read crumpled, abbreviated receipt lines (`YOG.NAT.X4` → "Plain yogurt"). That's Gemini. Reviewing an extraction and recalculating a date are text-only reasoning over data the backend already has. Those run on a local Ollama model, with no API cost and no rate limits. Because the services only see ports, swapping a provider means writing a new adapter. No service changes.

The models are configured in `application.properties` (`spring.ai.google.genai.chat.model`, `spring.ai.google.genai.chat.temperature`, `spring.ai.ollama.chat.options.model`). The Gemini API key comes from `GOOGLE_GENAI_API_KEY`. In Docker Compose, Ollama is reached on the host via `SPRING_AI_OLLAMA_BASE_URL=http://host.docker.internal:11434`.

#### How a call is built

All three adapters follow the same four steps:

1. **Render the prompt from a template.** Each adapter keeps its prompt as a text-block constant with `{placeholders}`. A builder fills them in using Spring AI's `PromptTemplate`: `PromptBuilder` for GenAI, `ReviewPromptBuilder` for both Ollama adapters. Lists are flattened into one line per item by small static formatters shared across adapters (`StorageSpotsPromptFormatter`, `ProductExtractionsPromptFormatter`, `ProductChangesPromptFormatter`). The prompt is grounded in the caller's real data, never free-form:
   - `{storageSpots}`: the user's actual storage spots (id, name, type), so the model can only suggest existing spot IDs.
   - `{productTypes}` / `{moneyCurrencies}`: the valid `ProductType`/`Currency` enum names, obtained from `product` through `ProductCategoriesLookUpPort`, so the model answers with values the domain accepts.
   - `{language}`: the language product names should be written in (resolved by `LanguageResolver`).
   - `{today}`: rendered from the injected `Clock`, so it's deterministic in tests.
2. **Ask for structured output.** A `BeanOutputConverter<T>` is created for the expected result type (`ReceiptExtraction`, `ReceiptExtractionToReview`, `LocalDate`). Its JSON schema is used twice: in the prompt, through the `{format}` placeholder, *and* as a hard constraint in the provider's chat options. GenAI takes `responseMimeType("application/json")` + `responseSchema(...)`, Ollama takes `outputSchema(...)`. The model is forced to return JSON matching the DTO rather than prose.
3. **Call the model.** GenAI attaches the image to the `UserMessage` as `Media`. On `process` it comes from the uploaded `MultipartFile`. On `reprocess` it's the stored image's bytes, fetched back from image storage, plus its MIME type. Ollama calls are text-only.
4. **Parse and validate the response** before returning anything to the service (see below).

```java
// GenAiShoppingReceiptProcessorAdapter.process(...)
var converter = new BeanOutputConverter<>(ReceiptExtraction.class);
String promptText = promptBuilder.build(PROCESS_TEMPLATE_PROMPT_TEXT, dto, converter);

var userMessage = UserMessage.builder()
        .text(promptText)
        .media(new Media(MimeType.valueOf(file.getContentType()), file.getResource()))
        .build();

GoogleGenAiChatOptions chatOptions = GoogleGenAiChatOptions.builder()
        .model(chatModel.getOptions().getModel())
        .responseMimeType("application/json")
        .responseSchema(converter.getJsonSchema())
        .build();

ChatResponse response = chatModel.call(new Prompt(userMessage, chatOptions));
return parser.parseAndValidate(response, converter);
```

#### The receipt pipeline: extract → review → re-extract

The three adapters combine into a two-model check on receipt extraction:

1. **Extract (Gemini).** `ProcessNewShoppingReceiptService` calls `process(...)`. The prompt asks for one entry per purchased unit (6 × milk becomes 6 entries) but one entry for a multipack sold as a single item, a per-unit price, an estimated shelf life → expiration date, and a suggested storage spot ID. If the image isn't a legible receipt, the model must fill `errorReason` and return an empty product list.
2. **Deterministic clean-up (no AI).** Before anything is trusted, the service fixes what the model can get wrong: `ExtractionDataRectifier` clamps a future purchase date to today and shifts every product's expiration date by the same number of days. `StorageSpotSuggestionResolver` replaces any suggested storage spot ID that isn't one of the user's spots with the first spot of the preferred type for that product type (from `product`'s mapping), or leaves it empty.
3. **Review (Ollama).** The cleaned extraction goes to `review(...)`. The reviewer is told to *catch mistakes, not redo the extraction*, and to flag a product only when it's clearly wrong. It returns the flagged products by `productIndex`, each with a one-sentence reason written for the end user. **The review is best-effort**: if the reviewer fails with any `InfrastructureException` (Ollama down, unparseable answer...), the service logs a warning and continues with no flags. A missing local model never blocks a receipt upload.
4. **Re-extract (Gemini).** The draft and its flagged products are returned to the client. Once the user has checked the draft, `ReProcessShoppingReceiptService` calls `reprocess(...)` with the stored image, the full previous extraction and the flagged products. The prompt tells Gemini to re-derive only the flagged products, copy the rest unchanged (fixing only invalid spot IDs), and not add, remove or reorder anything. The result goes through the same rectifier/resolver, then the products are registered through `ProductRegistrationPort`.

Moving a product is a single call: `MoveProductService` gathers the product, its purchase date (`ProductShoppingDateLookUpPort`), its full spot history and the old/new spots (`StorageSpotLookUpPort`), then calls the calculator. The prompt carries domain rules, e.g. colder spot → longer shelf life, warmer → shorter, never reset the clock (adjust from the latest expiration date), never return a date in the past unless the product has already expired. The returned date is passed to `Product.updateStorageSpot(...)`, so the aggregate still enforces its own invariants.

#### Response validation

The model's output is treated as untrusted input. It's checked before it reaches the application layer:

- `ReceiptExtractionParser` (GenAI) and the private `parseAndValidate` methods of the Ollama adapters reject a missing result, or text that is blank or doesn't start with `{` (or `"` for the date calculator, whose schema is a bare JSON string), with `UnparseableAiResponseException`. Conversion errors from `BeanOutputConverter.convert(...)` are wrapped the same way.
- For receipts, **an empty product list is the authoritative error signal**, not `errorReason`. The schema makes `errorReason` non-nullable, so the model fills it with placeholders (`""`, `"none"`...) even when nothing went wrong. An empty list raises `AiUnprocessableInputException`, using `errorReason` as the user-facing message when it contains a real explanation.

#### Errors and retries

Spring Retry is enabled in `shared/config/AppConfig` (`@EnableRetry`), and every adapter method is `@Retryable(retryFor = AiRetryableException.class)`: `process`/`reprocess`/`review` make 2 attempts with a 1 s delay, the expiration date calculator makes 3 attempts with backoff 2 s → 4 s. Only transient failures are retried. Input the model rejects is never retried.

Provider exceptions are translated into the shared `InfrastructureException` hierarchy, so the services and `GlobalExceptionHandler` never see Google- or Ollama-specific types:

- **GenAI**: `GenAiExceptionTranslator` maps the Google SDK exceptions (`GenAiIOException`, `ServerException`, `ClientException` + its HTTP code) to the types below. One thing to be aware of: `GoogleGenAiChatModel` doesn't let those exceptions through as they are. It catches every failure from the underlying Google SDK call and rethrows it wrapped in a generic `RuntimeException("Failed to generate content", cause)`. The specific error (rate limit, bad request, outage...) can only be reached through `getCause()`, not through the exception the adapter catches. If the translator only inspected that exception, nothing would ever match. Every Gemini failure would fall through to the `TicketProcessingException` fallback: a rate limit would never surface as `429`, and a temporary outage would return `500` instead of being retried. So `translate(...)` checks in three steps:

  ```java
  RuntimeException translated = translateKnownType(e);              // 1. the exception itself, in case it arrives unwrapped
  if (translated != null) return translated;

  RuntimeException translatedCause = translateKnownType(e.getCause()); // 2. the SDK exception wrapped by Spring AI
  if (translatedCause != null) return translatedCause;

  return new TicketProcessingException(...);                        // 3. unknown error
  ```

  It only looks one level deep, which matches how Spring AI currently wraps the error. If a future Spring AI version adds another wrapping layer, failures will start showing up as `TicketProcessingException`/`500`, and this is the place to check.
- **Ollama**: any `RuntimeException` from the call becomes `AiRetryableException` (the local server is unreachable or failed).

| Exception (`shared/infrastructure/exception`) | Raised when | Retried | HTTP |
|---|---|---|---|
| `AiRetryableException` | Network error (`GenAiIOException`), provider 5xx (`ServerException`), Ollama unreachable | Yes | `503` + `Retry-After: 5` |
| `UnparseableAiResponseException` (extends `AiRetryableException`) | Empty, non-JSON or unconvertible model output | Yes, the next answer may be valid | `502` |
| `AiRateLimitedException` | Gemini `429` | No | `429` |
| `AiUnprocessableInputException` | Other Gemini 4xx (image rejected), or no products extracted | No | `422` |
| `TicketProcessingException` | Gemini `401`/`403` (bad API key) or any unrecognized error | No | `500` |
| `ExpirationDateCalculationException` | Invalid input to the calculator (null DTO, empty spot history) | No | `500` (generic `InfrastructureException` handler) |

### Query ports (read side)

The admin back office needs paginated, filtered and aggregated views that span several modules: users with their spaces and receipts, receipts with their products, daily creation metrics. Building those from look-up ports would mean N+1 calls and joins in memory, and loading whole aggregates just to display a few columns. Query ports skip the domain model and read straight into flat DTOs. In CQRS terms, this is a separate read side.

| Port (`application/port/out`) | Adapter (`admin/infrastructure/persistence/jpa/`) | Used by | Answers |
|---|---|---|---|
| `admin` · `UsersQueryPort` | `user_query/UserQueryAdapter` | `GetUsersService` | daily registrations, paginated user list, user detail (roles, spaces, receipts) |
| `admin` · `ShoppingReceiptQueryPort` | `shopping_receipt_query/ShoppingReceiptQueryAdapter` | `GetShoppingReceiptsService` | daily receipt metrics, paginated receipts (by date range/space/creator), receipt detail with its products |
| `product` · `ProductQueryPort` | `product_query/ProductQueryAdapter` | `GetProductsService`, `GetProductTypesService`, `product`'s `SpaceProductsLookUpAdapter` | product lists/detail, counts per product type, daily product metrics, products in a space |

How they're implemented:

- **Criteria queries over other modules' JPA entities.** Adapters query `JpaUserEntity`, `JpaShoppingReceiptEntity`, `JpaProductEntity`... with Hibernate's `HibernateCriteriaBuilder` and select straight into constructor projections (`*Projection` records). A `mapper/` per adapter turns those into the port's DTOs.
- **Reusable building blocks** in `persistence/jpa/shared/`:
  - `SpecificationBuilder` composes optional filters as Spring Data `PredicateSpecification`s (`inDateRange`, `inSpace`, `createdBy`, `withProductType`...).
  - `MetricsQueryBuilder.build(rootEntityClass, filters, dateFieldName)` produces the generic "count per day" query behind every metrics endpoint: it casts the date field to a day, then groups and orders by it.
  - Per-adapter `query_builder/` classes hold each view's query plus its matching `countQuery` for pagination.
- **Pagination** is an `offset`/`limit` on the page query plus a separate count query, so `totalElements` counts every match, not just the current page. The count query is skipped when the page is empty.
- **Plain JDBC where Criteria can't do the job.** `JpaProductEntity` has `@SQLRestriction("deleted_at IS NULL")`, which silently hides soft-deleted products from every JPQL/Criteria query. A receipt's detail must still list them, flagged as deleted, so `ShoppingReceiptQueryAdapter` fetches those products with `NamedParameterJdbcTemplate`.
- The calling services are `@Transactional(readOnly = true)`.

This is a deliberate exception to module isolation, and it's safe because it's **read-only**: no aggregate is loaded or changed, so no invariant can be bypassed, and every write still goes through the owning module's use cases. The cost is coupling to other modules' table and entity shapes, so a schema change in `user`, `shopping_receipt` or `product` may also require updating these adapters.

### Exception hierarchy & HTTP error responses

Code never throws a raw `RuntimeException` for an expected failure. Every module throws a subtype of a small shared hierarchy, and **the type alone decides the HTTP status**. `GlobalExceptionHandler` (`shared/infrastructure/web/handler/`) translates it into an RFC 7807 `ProblemDetail` response. Services and controllers contain no status codes or `try/catch` blocks for this.

```
RuntimeException
├── FreshKeepException                       (shared/domain/exception)
│   ├── DomainException ─────────────────────────────────────── 400  "Business Rule Error"
│   │   ├── Invalid*Exception, NonExistent*Exception, ...         400  (validation / not found / rule broken)
│   │   │     e.g. InvalidEmailException, InvalidProductException → InvalidMoneyException → InvalidAmountException
│   │   ├── UnauthorizedException ────────────────────────────── 401  "Unauthorized Error"
│   │   │   └── InvalidCredentialsException
│   │   ├── ForbiddenException ───────────────────────────────── 403  "Forbidden Request"
│   │   │   └── DisabledAccountException
│   │   └── ConflictException ────────────────────────────────── 409  "Conflict Error"
│   │       └── AccountAlreadyExistsException, UserAlreadyExistsException,
│   │           ProductConcurrentlyModifiedException, ShoppingReceiptAlreadyConfirmedException,
│   │           SpaceNotAccessibleException, SpaceNotAvailableForParticipantException
│   └── InfrastructureException              (shared/infrastructure/exception) ── 500  "Server Error"
│       ├── *PersistenceException, ImageStorageException, ExpirationDateCalculationException  500
│       ├── IdentityMappingException, InvalidResolvedEntitiesException,
│       │   UserProvisioningPendingException, AdminProvisioningPendingException              500
│       ├── TicketProcessingException ─────────────────────────── 500  "Internal AI Server Error"
│       ├── AiUnprocessableInputException ─────────────────────── 422  "Unprocessable Ticket Data Error"
│       ├── AiRateLimitedException ────────────────────────────── 429  "AI Rate Limit Exceedance"
│       └── AiRetryableException ──────────────────────────────── 503  "AI Server Error" + Retry-After: 5
│           └── UnparseableAiResponseException ────────────────── 502  "AI Server Error"
└── ApplicationException                     (shared/application/exception) ── 500  "Application Server Error"
    └── MoveProductDataUnavailableException
```

**How to read it:**

- **`DomainException` = the caller's fault (4xx).** The request breaks a business rule or refers to something invalid. The handler returns the exception's message as `detail`, so messages are written for the client. Its four branches pick the status: the plain branch is `400`, and `UnauthorizedException`, `ForbiddenException` and `ConflictException` are `401`, `403` and `409`.
- **`InfrastructureException` = the system's fault (5xx).** A broken invariant or a failed external dependency (database, image storage, missing `User`/`Admin` row for an account...). The AI subtypes are the exception, with specific statuses so the client can react: `422` means change the image, `429` and `503` mean try again later (`503` carries a `Retry-After` header). Retry behaviour is detailed in [Errors and retries](#errors-and-retries).
- **`ApplicationException` = a use case couldn't gather the data it needs** from its own ports (e.g. a product's purchase date or storage spot history is missing when moving it). It sits outside `FreshKeepException` and maps to `500`.
- **Spring picks the most specific handler**, so a subtype with its own `@ExceptionHandler` overrides its parent's status (`UnparseableAiResponseException` returns `502` even though it's an `AiRetryableException`). A new exception only needs the right parent to get the right status. A new handler is only needed for a new status.

Some mappings are deliberate choices worth knowing about:

- There's no `404` type: `NonExistent*Exception`s (`NonExistentProductException`, `NonExistentSpaceException`...) are plain `DomainException`s and return `400`.
- `SpaceNotAccessibleException` (user isn't a participant of the space) is a `ConflictException`, so it returns `409`, not `403`.

**Errors raised outside the domain hierarchy** are handled too, always as `ProblemDetail`:

| Source | Status | Title |
|---|---|---|
| Bean Validation on a request body (`MethodArgumentNotValidException`) | 400 | "Validation Error In Body Data", plus an `errors` map of field → message |
| Bean Validation on a path/query parameter (`ConstraintViolationException`) | 400 | "Validation Error in Parameter", plus an `errors` map |
| Missing or malformed JSON body (`HttpMessageNotReadableException`) | 400 | "Message Not Readable" |
| Parameter of the wrong type, e.g. a non-UUID ID (`MethodArgumentTypeMismatchException`) | 400 | "Type Mismatch Error" |
| Unwrapped `DataAccessException` from Spring Data | 500 | "Server Error", with a generic detail: the raw database message is logged, never returned |
| No or invalid JWT (`CustomAuthenticationEntryPoint`, security filter chain) | 401 | "Unauthorized" |
| Authenticated but missing the role (`CustomAccessDeniedHandler`) | 403 | "Forbidden" |

The last two are produced by Spring Security before the request reaches a controller, so they don't go through `GlobalExceptionHandler`. They return the same `ProblemDetail` shape so the client only has one error format to parse:

```json
{
  "type": "about:blank",
  "title": "Conflict Error",
  "status": 409,
  "detail": "A user account with the email address jane@example.com already exists.",
  "instance": "/api/v1/auth/register/user"
}
```

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
