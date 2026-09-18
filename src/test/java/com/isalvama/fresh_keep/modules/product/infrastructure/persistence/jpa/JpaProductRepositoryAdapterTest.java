package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.product.domain.exception.NonExistentProductException;
import com.isalvama.fresh_keep.modules.product.domain.exception.ProductConcurrentlyModifiedException;
import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Currency;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Money;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductName;
import com.isalvama.fresh_keep.modules.product.infrastructure.exception.ProductPersistenceException;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductEntity;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.mapper.ProductMapper;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaProductRepositoryAdapter.class, ProductMapper.class})
class JpaProductRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private JpaProductRepositoryAdapter adapter;

    @Autowired
    private JpaProductSpringDataRepository jpaProductRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private UUID storageSpotId;
    private UUID shoppingReceiptId;
    private UUID spaceId;

    @BeforeEach
    void setUp() {
        spaceId = UUID.randomUUID();
        storageSpotId = UUID.randomUUID();
        shoppingReceiptId = UUID.randomUUID();

        insertSpace(spaceId, "Kitchen", "🏠");
        insertStorageSpot(storageSpotId, "Fridge", "FRIDGE", spaceId);
        insertShoppingReceipt(shoppingReceiptId);
    }

    private Product createProduct(String name, BigDecimal priceAmount, String currency) {
        Product product = Product.create(
                ProductName.from(name),
                LocalDate.now().plusDays(7),
                StorageSpotId.of(storageSpotId),
                ProductType.DAIRY,
                ShoppingReceiptId.of(shoppingReceiptId),
                priceAmount != null ? Money.from(priceAmount, currency) : null
        );
        product.setActualStorageSpotId(StorageSpotId.of(storageSpotId));
        return product;
    }

    @Nested
    class Save {

        @Test
        void shouldPersistNewProduct() {
            Product product = createProduct("Milk", BigDecimal.valueOf(1.5), "USD");

            adapter.save(product);
            jpaProductRepository.flush();

            JpaProductEntity saved = jpaProductRepository.findById(product.getId().value()).orElseThrow();
            assertEquals(product.getId().value(), saved.getId());
            assertEquals("Milk", saved.getName());
            assertEquals(product.getExpirationDate(), saved.getExpirationDate());
            assertEquals(storageSpotId, saved.getSuggestedStorageSpotId());
            assertEquals(storageSpotId, saved.getActualStorageSpotId());
            assertEquals(ProductType.DAIRY, saved.getProductType());
            assertEquals(shoppingReceiptId, saved.getShoppingReceiptId());
            assertEquals(0, BigDecimal.valueOf(1.5).compareTo(saved.getPrice()));
            assertEquals(Currency.USD, saved.getCurrency());
        }

        @Test
        void shouldUpdateExistingProductWithoutChangingImmutableFields() {
            Product original = createProduct("Milk", BigDecimal.valueOf(1.5), "USD");
            adapter.save(original);
            jpaProductRepository.flush();
            Instant initialLastUpdatedAt = jdbcTemplate.queryForObject(
                    "SELECT last_updated_at FROM products WHERE id = ?", Instant.class, original.getId().value());

            UUID updatedStorageSpotId = UUID.randomUUID();
            insertStorageSpot(updatedStorageSpotId, "Pantry", "PANTRY", spaceId);
            Product updated = Product.reconstitute(
                    original.getId(),
                    ProductName.from("Updated Milk"),
                    LocalDate.now().plusDays(14),
                    StorageSpotId.of(UUID.randomUUID()),
                    ProductType.PANTRY,
                    ShoppingReceiptId.of(UUID.randomUUID()),
                    Money.from(BigDecimal.valueOf(9.99), "EUR")
            );
            updated.setActualStorageSpotId(StorageSpotId.of(updatedStorageSpotId));

            adapter.save(updated);
            jpaProductRepository.flush();
            entityManager.clear();

            JpaProductEntity saved = jpaProductRepository.findById(original.getId().value()).orElseThrow();
            assertEquals("Updated Milk", saved.getName());
            assertEquals(updated.getExpirationDate(), saved.getExpirationDate());
            assertEquals(updatedStorageSpotId, saved.getActualStorageSpotId());
            assertEquals(ProductType.PANTRY, saved.getProductType());
            assertEquals(storageSpotId, saved.getSuggestedStorageSpotId());
            assertEquals(shoppingReceiptId, saved.getShoppingReceiptId());
            assertEquals(0, BigDecimal.valueOf(1.5).compareTo(saved.getPrice()));
            assertEquals(Currency.USD, saved.getCurrency());
            Instant updatedLastUpdatedAt = jdbcTemplate.queryForObject(
                    "SELECT last_updated_at FROM products WHERE id = ?", Instant.class, original.getId().value());
            assertTrue(updatedLastUpdatedAt.isAfter(initialLastUpdatedAt));
        }
    }

    @Nested
    class SaveAll {

        @Test
        void shouldPersistAllProductsSuccessfully() {
            Product milk = createProduct("Milk", BigDecimal.valueOf(1.5), "USD");
            Product yogurt = createProduct("Yogurt", BigDecimal.valueOf(2.0), "USD");

            adapter.saveAll(List.of(milk, yogurt));

            Optional<JpaProductEntity> savedMilk = jpaProductRepository.findById(milk.getId().value());
            assertTrue(savedMilk.isPresent());
            JpaProductEntity entity = savedMilk.orElseThrow();
            assertEquals("Milk", entity.getName());
            assertEquals(milk.getExpirationDate(), entity.getExpirationDate());
            assertEquals(storageSpotId, entity.getSuggestedStorageSpotId());
            assertEquals(storageSpotId, entity.getActualStorageSpotId());
            assertEquals(ProductType.DAIRY, entity.getProductType());
            assertEquals(shoppingReceiptId, entity.getShoppingReceiptId());
            assertEquals(0, BigDecimal.valueOf(1.5).compareTo(entity.getPrice()));
            assertEquals(Currency.USD, entity.getCurrency());

            assertTrue(jpaProductRepository.findById(yogurt.getId().value()).isPresent());
        }

        @Test
        void shouldThrowProductPersistenceExceptionWhenForeignKeyIsInvalid() {
            Product invalidProduct = Product.create(
                    ProductName.from("Milk"),
                    LocalDate.now().plusDays(7),
                    StorageSpotId.of(storageSpotId),
                    ProductType.DAIRY,
                    ShoppingReceiptId.create(),
                    Money.from(BigDecimal.valueOf(1.5), "USD")
            );
            invalidProduct.setActualStorageSpotId(StorageSpotId.of(storageSpotId));

            Exception exception = assertThrows(ProductPersistenceException.class,
                    () -> adapter.saveAll(List.of(invalidProduct)));

            assertTrue(exception.getMessage().contains(invalidProduct.getId().toString()));
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnProductWhenItExists() {
            Product milk = createProduct("Milk", BigDecimal.valueOf(1.5), "USD");
            adapter.saveAll(List.of(milk));

            Optional<Product> found = adapter.findById(milk.getId());

            assertTrue(found.isPresent());
            assertEquals(milk.getId(), found.get().getId());
            assertEquals("Milk", found.get().getName().value());
        }

        @Test
        void shouldReturnEmptyWhenProductDoesNotExist() {
            Optional<Product> found = adapter.findById(ProductId.create());

            assertTrue(found.isEmpty());
        }

        @Test
        void shouldReturnEmptyWhenProductIsSoftDeleted() {
            Product milk = createProduct("Milk", BigDecimal.valueOf(1.5), "USD");
            adapter.saveAll(List.of(milk));
            softDeleteProduct(milk.getId().value());
            entityManager.clear();

            Optional<Product> found = adapter.findById(milk.getId());

            assertTrue(found.isEmpty());
        }
    }

    @Nested
    class FindAllById {

        @Test
        void shouldReturnAllMatchingProducts() {
            Product milk = createProduct("Milk", BigDecimal.valueOf(1.5), "USD");
            Product yogurt = createProduct("Yogurt", BigDecimal.valueOf(2.0), "USD");
            adapter.saveAll(List.of(milk, yogurt));

            List<Product> found = adapter.findAllById(List.of(milk.getId(), yogurt.getId()));

            assertEquals(2, found.size());
        }

        @Test
        void shouldReturnEmptyListWhenNoneMatch() {
            List<Product> found = adapter.findAllById(List.of(ProductId.create(), ProductId.create()));

            assertTrue(found.isEmpty());
        }

        @Test
        void shouldExcludeSoftDeletedProducts() {
            Product milk = createProduct("Milk", BigDecimal.valueOf(1.5), "USD");
            Product yogurt = createProduct("Yogurt", BigDecimal.valueOf(2.0), "USD");
            adapter.saveAll(List.of(milk, yogurt));
            softDeleteProduct(milk.getId().value());

            List<Product> found = adapter.findAllById(List.of(milk.getId(), yogurt.getId()));

            assertEquals(1, found.size());
            assertEquals(yogurt.getId(), found.getFirst().getId());
        }

        @Test
        void shouldReturnOnlyExistingProductsWhenSomeIdsDoNotExist() {
            Product milk = createProduct("Milk", BigDecimal.valueOf(1.5), "USD");
            adapter.saveAll(List.of(milk));

            List<Product> found = adapter.findAllById(List.of(milk.getId(), ProductId.create()));

            assertEquals(1, found.size());
            assertEquals(milk.getId(), found.getFirst().getId());
        }
    }

    @Nested
    class Delete {

        private final Clock fixedClock = Clock.fixed(Instant.parse("2026-09-14T10:00:00Z"), ZoneOffset.UTC);

        @Test
        void shouldSoftDeleteProductSuccessfully() {
            Product milk = createProduct("Milk", BigDecimal.valueOf(1.5), "USD");
            adapter.saveAll(List.of(milk));

            adapter.delete(milk, fixedClock);
            entityManager.clear();

            assertTrue(adapter.findById(milk.getId()).isEmpty());
            Instant deletedAt = jdbcTemplate.queryForObject(
                    "SELECT deleted_at FROM products WHERE id = ?", Instant.class, milk.getId().value());
            assertEquals(fixedClock.instant(), deletedAt);
        }

        @Test
        void shouldThrowNonExistentProductExceptionWhenProductDoesNotExist() {
            Product neverPersisted = createProduct("Ghost", BigDecimal.valueOf(1.0), "USD");

            assertThrows(NonExistentProductException.class, () -> adapter.delete(neverPersisted, fixedClock));
        }

        @Test
        void shouldThrowNonExistentProductExceptionWhenProductIsAlreadyDeleted() {
            Product milk = createProduct("Milk", BigDecimal.valueOf(1.5), "USD");
            adapter.saveAll(List.of(milk));
            adapter.delete(milk, fixedClock);
            entityManager.clear();

            assertThrows(NonExistentProductException.class, () -> adapter.delete(milk, fixedClock));
        }
    }

    @Nested
    class DeleteAll {

        private final Clock fixedClock = Clock.fixed(Instant.parse("2026-09-14T10:00:00Z"), ZoneOffset.UTC);

        @Test
        void shouldSoftDeleteAllProductsSuccessfully() {
            Product milk = createProduct("Milk", BigDecimal.valueOf(1.5), "USD");
            Product yogurt = createProduct("Yogurt", BigDecimal.valueOf(2.0), "USD");
            adapter.saveAll(List.of(milk, yogurt));

            adapter.deleteAll(List.of(milk, yogurt), fixedClock);

            assertTrue(adapter.findAllById(List.of(milk.getId(), yogurt.getId())).isEmpty());
            Instant milkDeletedAt = jdbcTemplate.queryForObject(
                    "SELECT deleted_at FROM products WHERE id = ?", Instant.class, milk.getId().value());
            Instant yogurtDeletedAt = jdbcTemplate.queryForObject(
                    "SELECT deleted_at FROM products WHERE id = ?", Instant.class, yogurt.getId().value());
            assertEquals(fixedClock.instant(), milkDeletedAt);
            assertEquals(fixedClock.instant(), yogurtDeletedAt);
        }

        @Test
        void shouldThrowAndLeaveEverythingUntouchedWhenOneProductWasAlreadyDeletedConcurrently() {
            Product milk = createProduct("Milk", BigDecimal.valueOf(1.5), "USD");
            Product yogurt = createProduct("Yogurt", BigDecimal.valueOf(2.0), "USD");
            adapter.saveAll(List.of(milk, yogurt));
            softDeleteProduct(milk.getId().value());

            assertThrows(ProductConcurrentlyModifiedException.class,
                    () -> adapter.deleteAll(List.of(milk, yogurt), fixedClock));

            assertTrue(adapter.findById(yogurt.getId()).isPresent());
        }

        @Test
        void shouldThrowWhenAnyProductNeverExisted() {
            Product milk = createProduct("Milk", BigDecimal.valueOf(1.5), "USD");
            Product neverPersisted = createProduct("Ghost", BigDecimal.valueOf(1.0), "USD");
            adapter.saveAll(List.of(milk));

            assertThrows(ProductConcurrentlyModifiedException.class,
                    () -> adapter.deleteAll(List.of(milk, neverPersisted), fixedClock));

            assertTrue(adapter.findById(milk.getId()).isPresent());
        }
    }

    private void softDeleteProduct(UUID id) {
        jdbcTemplate.update("UPDATE products SET deleted_at = ? WHERE id = ?", Timestamp.from(Instant.now()), id);
    }

    private void insertSpace(UUID id, String name, String emoji) {
        jdbcTemplate.update(
                "INSERT INTO spaces (id, name, emoji) VALUES (?, ?, ?)",
                id, name, emoji
        );
    }

    private void insertStorageSpot(UUID id, String name, String type, UUID spaceId) {
        jdbcTemplate.update(
                "INSERT INTO storage_spots (id, name, storage_spot_type, space_id) VALUES (?, ?, ?, ?)",
                id, name, type, spaceId
        );
    }

    private void insertShoppingReceipt(UUID id) {
        jdbcTemplate.update("INSERT INTO shopping_receipts (id, status) VALUES (?, 'DRAFT')", id);
    }
}
