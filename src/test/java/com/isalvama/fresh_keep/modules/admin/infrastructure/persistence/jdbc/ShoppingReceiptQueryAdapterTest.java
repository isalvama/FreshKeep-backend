package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jdbc;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.DailyReceiptSummaryDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptDetailDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptSummaryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ShoppingReceiptQueryAdapter.class)
class ShoppingReceiptQueryAdapterTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private ShoppingReceiptQueryAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID creatorId;
    private UUID spaceId;

    @BeforeEach
    void setUp() {
        creatorId = UUID.randomUUID();
        spaceId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, ?)", accountId, "receipt-owner@email.com", "hash");
        jdbcTemplate.update("INSERT INTO users (id, account_id, email, username) VALUES (?, ?, ?, ?)",
                creatorId, accountId, "receipt-owner@email.com", "receipt-owner");
        jdbcTemplate.update("INSERT INTO spaces (id, name, emoji) VALUES (?, ?, ?)", spaceId, "Kitchen", "🏠");
    }

    @Test
    void getShoppingReceiptMetrics_groupsByPurchaseDateAndAppliesFilters() {
        insertReceipt(LocalDate.of(2026, 4, 10), creatorId, spaceId, 1);
        insertReceipt(LocalDate.of(2026, 4, 10), creatorId, spaceId, 2);
        insertReceipt(LocalDate.of(2026, 4, 11), creatorId, spaceId, 3);

        List<DailyReceiptSummaryDto> result = adapter.getShoppingReceiptMetrics(
                LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 10), spaceId, creatorId);

        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2026, 4, 10), result.getFirst().date());
        assertEquals(2, result.getFirst().totalReceipts());
    }

    @Test
    void findReceipts_returnsNewestFirstAndAppliesDateRange() {
        UUID firstId = insertReceipt(LocalDate.of(2026, 5, 10), creatorId, spaceId, 1);
        UUID secondId = insertReceipt(LocalDate.of(2026, 5, 11), creatorId, spaceId, 2);

        List<ReceiptSummaryDto> result = adapter.findReceipts(
                LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 11), spaceId, creatorId);

        assertEquals(List.of(secondId, firstId), result.stream().map(ReceiptSummaryDto::id).toList());
    }

    @Test
    void findReceiptById_returnsReceiptWithProductsAndImage() {
        UUID imageId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO receipt_images (id, asset_id, mime_type) VALUES (?, ?, ?)", imageId, "asset-1", "image/jpeg");
        UUID receiptId = insertReceipt(LocalDate.of(2026, 6, 10), creatorId, spaceId, 1);
        jdbcTemplate.update("UPDATE shopping_receipts SET receipt_image_id = ?, store_name = ? WHERE id = ?", imageId, "Fresh Store", receiptId);
        UUID productId = UUID.randomUUID();
        UUID storageSpotId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO storage_spots (id, name, storage_spot_type, space_id) VALUES (?, ?, ?, ?)",
                storageSpotId, "Fridge", "FRIDGE", spaceId);
        jdbcTemplate.update("INSERT INTO products (id, name, expiration_date, actual_storage_spot_id, product_type, shopping_receipt_id, price, currency) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                productId, "Milk", LocalDate.of(2026, 6, 20), storageSpotId, "DAIRY", receiptId, BigDecimal.valueOf(1.5), "USD");

        ReceiptDetailDto result = adapter.findReceiptById(receiptId).orElseThrow();

        assertEquals("receipt-owner", result.creatorUsername());
        assertEquals("Fresh Store", result.storeName());
        assertEquals("asset-1", result.receiptImageAssetId());
        assertEquals(productId, result.products().getFirst().id());
    }

    @Test
    void findReceiptById_returnsEmptyForMissingReceipt() {
        assertTrue(adapter.findReceiptById(UUID.randomUUID()).isEmpty());
    }

    private UUID insertReceipt(LocalDate purchaseDate, UUID creator, UUID space, int sequence) {
        UUID receiptId = UUID.randomUUID();
        Timestamp createdAt = Timestamp.valueOf(purchaseDate.atStartOfDay().plusHours(sequence));
        jdbcTemplate.update("INSERT INTO shopping_receipts (id, creator_id, space_id, purchase_date, created_at, status) VALUES (?, ?, ?, ?, ?, 'DRAFT')",
                receiptId, creator, space, createdAt, createdAt);
        return receiptId;
    }
}
