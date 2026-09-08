package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Currency;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Money;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductName;
import com.isalvama.fresh_keep.modules.product.infrastructure.exception.ProductPersistenceException;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductEntity;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.mapper.ProductMapper;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
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
import java.time.LocalDate;
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

    private UUID storageSpotId;
    private UUID shoppingReceiptId;

    @BeforeEach
    void setUp() {
        UUID spaceId = UUID.randomUUID();
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
        jdbcTemplate.update("INSERT INTO shopping_receipts (id) VALUES (?)", id);
    }
}
