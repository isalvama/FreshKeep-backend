package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.application.service.dto.ProductMove;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Money;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductName;
import com.isalvama.fresh_keep.modules.product.infrastructure.exception.ProductStorageSpotHistoryPersistenceException;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductStorageSpotHistoryEntity;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.mapper.ProductMapper;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.mapper.ProductStorageSpotHistoryMapper;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaProductRepositoryAdapter.class, ProductMapper.class,
        JpaProductStorageSpotHistoryRepositoryAdapter.class, ProductStorageSpotHistoryMapper.class
})
class JpaProductStorageSpotHistoryRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private JpaProductRepositoryAdapter productRepositoryAdapter;

    @Autowired
    private JpaProductStorageSpotHistoryRepositoryAdapter adapter;

    @Autowired
    private JpaProductStorageSpotHistorySpringDataRepository jpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID creatorId;
    private UUID storageSpotId;
    private UUID shoppingReceiptId;

    @BeforeEach
    void setUp() {
        UUID accountId = UUID.randomUUID();
        UUID spaceId = UUID.randomUUID();
        creatorId = UUID.randomUUID();
        storageSpotId = UUID.randomUUID();
        shoppingReceiptId = UUID.randomUUID();

        insertAccount(accountId, "creator@email.com", "password-hash");
        insertUser(creatorId, accountId, "creator@email.com", "creatorUserName");
        insertSpace(spaceId, "Kitchen", "🏠");
        insertStorageSpot(storageSpotId, "Fridge", "FRIDGE", spaceId);
        insertShoppingReceipt(shoppingReceiptId);
    }

    private Product createAndPersistProduct(String name) {
        Product product = Product.create(
                ProductName.from(name),
                LocalDate.now().plusDays(7),
                StorageSpotId.of(storageSpotId),
                ProductType.DAIRY,
                ShoppingReceiptId.of(shoppingReceiptId),
                Money.from(BigDecimal.valueOf(1.5), "USD")
        );
        product.setActualStorageSpotId(StorageSpotId.of(storageSpotId));
        productRepositoryAdapter.saveAll(List.of(product));
        return product;
    }

    @Nested
    class SaveAll {

        @Test
        void shouldPersistProductStorageSpotHistorySuccessfully() {
            Product milk = createAndPersistProduct("Milk");

            adapter.saveAll(List.of(milk), creatorId);

            List<JpaProductStorageSpotHistoryEntity> histories = jpaRepository.findAll();
            assertEquals(1, histories.size());
            JpaProductStorageSpotHistoryEntity entity = histories.getFirst();
            assertEquals(milk.getId().value(), entity.getProductId());
            assertEquals(creatorId, entity.getUserId());
            assertEquals(milk.getExpirationDate(), entity.getNewExpirationDate());
            assertEquals(storageSpotId, entity.getNewStorageSpotId());
            assertNotNull(entity.getChangedAt());
        }

        @Test
        void shouldPersistOneHistoryEntryPerProduct() {
            Product milk = createAndPersistProduct("Milk");
            Product yogurt = createAndPersistProduct("Yogurt");

            adapter.saveAll(List.of(milk, yogurt), creatorId);

            assertEquals(2, jpaRepository.findAll().size());
        }

        @Test
        void shouldThrowProductStorageSpotHistoryPersistenceExceptionWhenForeignKeyIsInvalid() {
            Product milk = createAndPersistProduct("Milk");
            UUID nonExistentCreatorId = UUID.randomUUID();

            Exception exception = assertThrows(ProductStorageSpotHistoryPersistenceException.class,
                    () -> adapter.saveAll(List.of(milk), nonExistentCreatorId));

            assertTrue(exception.getMessage().contains(milk.getId().toString()));
        }
    }

    @Nested
    class FindByProductId {

        @Test
        void shouldReturnProductMovesForTheRequestedProduct() {
            Product milk = createAndPersistProduct("Milk");
            adapter.saveAll(List.of(milk), creatorId);

            List<ProductMove> result = adapter.findByProductId(milk.getId());

            assertEquals(1, result.size());
            ProductMove move = result.getFirst();
            assertEquals(creatorId.toString(), move.userId());
            assertEquals(storageSpotId.toString(), move.newStorageSpotId());
            assertEquals(milk.getExpirationDate(), move.newExpirationDate());
            assertNotNull(move.changedAt());
        }

        @Test
        void shouldReturnEmptyListWhenProductHasNoHistory() {
            Product milk = createAndPersistProduct("Milk");

            assertTrue(adapter.findByProductId(milk.getId()).isEmpty());
        }
    }

    private void insertAccount(UUID id, String email, String passwordHash) {
        jdbcTemplate.update(
                "INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, ?)",
                id, email, passwordHash
        );
    }

    private void insertUser(UUID id, UUID accountId, String email, String username) {
        jdbcTemplate.update(
                "INSERT INTO users (id, account_id, email, username) VALUES (?, ?, ?, ?)",
                id, accountId, email, username
        );
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
