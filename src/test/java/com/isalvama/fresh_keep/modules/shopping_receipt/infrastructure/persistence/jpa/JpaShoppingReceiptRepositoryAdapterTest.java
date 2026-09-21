package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceiptStatus;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ReceiptImageId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.exception.ShoppingReceiptPersistenceException;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaShoppingReceiptEntity;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.mapper.ShoppingReceiptMapper;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import com.isalvama.fresh_keep.shared.config.AppConfig;
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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaShoppingReceiptRepositoryAdapter.class, ShoppingReceiptMapper.class, AppConfig.class})
class JpaShoppingReceiptRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private JpaShoppingReceiptRepositoryAdapter adapter;

    @Autowired
    private JpaSpringDataShoppingReceiptRepository jpaRepository;

    @Autowired
    private Clock clock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String id = ShoppingReceiptId.create().toString();
    private final String creatorId = UserId.create().toString();
    private final String spaceId = SpaceId.create().toString();
    private final String receiptImageId = ReceiptImageId.create().toString();
    private final LocalDate purchaseDate = LocalDate.of(2026, 9, 6);
    private final String storeName = "Store Name";

    @BeforeEach
    void setUp() {
        UUID accountId = UUID.randomUUID();
        insertAccount(accountId, "creator@email.com", "password-hash");
        insertUser(UUID.fromString(creatorId), accountId, "creator@email.com", "creatorUserName");
        insertSpace(UUID.fromString(spaceId), "Kitchen", "🏠", UUID.fromString(creatorId));
        insertReceiptImage(UUID.fromString(receiptImageId), "asset-id", "image/jpeg");
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

    private void insertSpace(UUID id, String name, String emoji, UUID creatorId) {
        jdbcTemplate.update(
                "INSERT INTO spaces (id, name, emoji, creator_id) VALUES (?, ?, ?, ?)",
                id, name, emoji, creatorId
        );
    }

    private void insertReceiptImage(UUID id, String assetId, String mimeType) {
        jdbcTemplate.update(
                "INSERT INTO receipt_images (id, asset_id, mime_type) VALUES (?, ?, ?)",
                id, assetId, mimeType
        );
    }

    @Nested
    class Save{

        @Test
        void shouldSaveShoppingReceiptSuccessfullyAndUpdateCreatedAt(){
            adapter.save(ShoppingReceipt.reconstitute(ShoppingReceiptId.from(id), UserId.from(creatorId), SpaceId.from(spaceId), ReceiptImageId.from(receiptImageId), purchaseDate, storeName, ShoppingReceiptStatus.DRAFT));

            Optional<JpaShoppingReceiptEntity> result = jpaRepository.findById(UUID.fromString(id));

            assertTrue(result.isPresent());
            JpaShoppingReceiptEntity resultingEntity = result.orElseThrow(RuntimeException::new);
            assertEquals(resultingEntity.getId().toString(), id);
            assertEquals(resultingEntity.getCreatorId().toString(), creatorId);
            assertEquals(resultingEntity.getSpaceId().toString(), spaceId);
            assertEquals(resultingEntity.getReceiptImageId().toString(), receiptImageId);
            assertEquals(resultingEntity.getPurchaseDate().atZone(ZoneOffset.UTC).toLocalDate(), purchaseDate);
            assertEquals(resultingEntity.getStoreName(), storeName);
            assertEquals(ShoppingReceiptStatus.DRAFT, resultingEntity.getStatus());
            assertTrue(resultingEntity.getCreatedAt().isBefore(Instant.now().plus(1, ChronoUnit.MINUTES)));
            assertTrue(resultingEntity.getCreatedAt().isAfter(Instant.now().minus(1, ChronoUnit.MINUTES)));
        }

        @Test
        void shouldThrowShoppingReceiptPersistenceExceptionWhenForeignKeyIsInvalid() {
            ShoppingReceipt invalidReceipt = ShoppingReceipt.reconstitute(
                    ShoppingReceiptId.create(),
                    UserId.from(creatorId),
                    SpaceId.from(spaceId),
                    ReceiptImageId.create(),
                    purchaseDate,
                    storeName,
                    ShoppingReceiptStatus.DRAFT
            );

            Exception exception = assertThrows(ShoppingReceiptPersistenceException.class, () -> adapter.save(invalidReceipt));

            assertTrue(exception.getMessage().contains(invalidReceipt.getId().toString()));
        }
    }

    @Nested
    class GetShoppingDate {

        @Test
        void shouldReturnTheShoppingDateForAnExistingReceipt() {
            Instant shoppingDate = Instant.parse("2026-09-06T12:00:00Z");
            insertShoppingReceipt(UUID.fromString(id), shoppingDate);

            Optional<LocalDate> result = adapter.getShoppingDate(ShoppingReceiptId.from(id));

            assertEquals(Optional.of(LocalDate.ofInstant(shoppingDate, ZoneOffset.UTC)), result);
        }

        @Test
        void shouldReturnEmptyWhenTheReceiptDoesNotExist() {
            Optional<LocalDate> result = adapter.getShoppingDate(ShoppingReceiptId.create());

            assertTrue(result.isEmpty());
        }

    }

    private void insertShoppingReceipt(UUID id, Instant purchaseDate) {
        jdbcTemplate.update(
                "INSERT INTO shopping_receipts (id, purchase_date, status) VALUES (?, ?, 'DRAFT')",
                id, java.sql.Timestamp.from(purchaseDate)
        );
    }
}
