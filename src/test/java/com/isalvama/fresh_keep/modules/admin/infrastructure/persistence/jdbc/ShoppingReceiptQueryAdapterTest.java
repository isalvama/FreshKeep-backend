package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jdbc;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptDetailDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptSummaryDto;
import org.junit.jupiter.api.BeforeEach;
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

    private static final LocalDate SEP_10 = LocalDate.of(2026, 9, 10);

    @Autowired
    private ShoppingReceiptQueryAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID kitchenId;
    private UUID storageSpotId;
    private UUID aliceId;

    @BeforeEach
    void setUp() {
        kitchenId = insertSpace("Kitchen");
        storageSpotId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO storage_spots (id, name, storage_spot_type, space_id) VALUES (?, ?, 'FRIDGE', ?)",
                storageSpotId, "Fridge", kitchenId);
        aliceId = insertUser("alice@email.com", "alice");
    }

    @Test
    void findReceipts_filtersByPurchaseDateInclusively() {
        UUID before = insertReceipt(aliceId, kitchenId, SEP_10.minusDays(1), SEP_10);
        UUID first = insertReceipt(aliceId, kitchenId, SEP_10, SEP_10.plusDays(5));
        UUID last = insertReceipt(aliceId, kitchenId, SEP_10.plusDays(2), SEP_10.plusDays(2));
        UUID after = insertReceipt(aliceId, kitchenId, SEP_10.plusDays(3), SEP_10.plusDays(1));

        List<UUID> ids = ids(adapter.findReceipts(SEP_10, SEP_10.plusDays(2), null, null));

        assertEquals(2, ids.size());
        assertTrue(ids.containsAll(List.of(first, last)));
        assertFalse(ids.contains(before));
        assertFalse(ids.contains(after));
    }

    @Test
    void findReceipts_filtersBySpace() {
        UUID garageId = insertSpace("Garage");
        UUID kitchenReceipt = insertReceipt(aliceId, kitchenId, SEP_10, SEP_10);
        insertReceipt(aliceId, garageId, SEP_10, SEP_10);

        assertEquals(List.of(kitchenReceipt), ids(adapter.findReceipts(SEP_10, SEP_10, kitchenId, null)));
    }

    @Test
    void findReceipts_filtersByCreator() {
        UUID bobId = insertUser("bob@email.com", "bob");
        UUID aliceReceipt = insertReceipt(aliceId, kitchenId, SEP_10, SEP_10);
        insertReceipt(bobId, kitchenId, SEP_10, SEP_10);

        assertEquals(List.of(aliceReceipt), ids(adapter.findReceipts(SEP_10, SEP_10, null, aliceId)));
    }

    @Test
    void findReceipts_mapsStoreAndDates() {
        UUID receiptId = insertReceipt(aliceId, kitchenId, SEP_10, SEP_10.plusDays(1));
        jdbcTemplate.update("UPDATE shopping_receipts SET store_name = 'SuperMart' WHERE id = ?", receiptId);

        ReceiptSummaryDto receipt = adapter.findReceipts(SEP_10, SEP_10, null, null).getFirst();

        assertEquals(aliceId, receipt.creatorId());
        assertEquals(kitchenId, receipt.spaceId());
        assertEquals("SuperMart", receipt.storeName());
        assertNotNull(receipt.purchaseDate());
        assertNotNull(receipt.createdAt());
    }

    @Test
    void findReceiptById_returnsCreatorSpaceAndProductsInCreationOrder() {
        UUID receiptId = insertReceipt(aliceId, kitchenId, SEP_10, SEP_10);
        UUID milkId = insertProduct(receiptId, "Milk", 1);
        UUID breadId = insertProduct(receiptId, "Bread", 2);

        ReceiptDetailDto receipt = adapter.findReceiptById(receiptId).orElseThrow();

        assertEquals("alice@email.com", receipt.creatorEmail());
        assertEquals("alice", receipt.creatorUsername());
        assertEquals("Kitchen", receipt.spaceName());
        assertEquals(List.of(milkId, breadId), receipt.products().stream().map(p -> p.id()).toList());
    }

    @Test
    void findReceiptById_includesDeletedProducts() {
        UUID receiptId = insertReceipt(aliceId, kitchenId, SEP_10, SEP_10);
        UUID milkId = insertProduct(receiptId, "Milk", 1);
        UUID breadId = insertProduct(receiptId, "Bread", 2);
        jdbcTemplate.update("UPDATE products SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?", breadId);

        ReceiptDetailDto receipt = adapter.findReceiptById(receiptId).orElseThrow();

        assertEquals(List.of(milkId, breadId), receipt.products().stream().map(p -> p.id()).toList());
    }

    @Test
    void findReceiptById_isEmptyForAnUnknownReceipt() {
        assertTrue(adapter.findReceiptById(UUID.randomUUID()).isEmpty());
    }

    private static List<UUID> ids(List<ReceiptSummaryDto> receipts) {
        return receipts.stream().map(ReceiptSummaryDto::id).toList();
    }

    private UUID insertSpace(String name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO spaces (id, name, emoji) VALUES (?, ?, '🏠')", id, name);
        return id;
    }

    private UUID insertUser(String email, String username) {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, 'hash')", accountId, email);
        jdbcTemplate.update("INSERT INTO users (id, account_id, email, username) VALUES (?, ?, ?, ?)",
                userId, accountId, email, username);
        return userId;
    }

    /** Purchased and uploaded at noon (JVM time) on the given days. */
    private UUID insertReceipt(UUID creatorId, UUID spaceId, LocalDate purchased, LocalDate uploaded) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO shopping_receipts (id, creator_id, space_id, purchase_date, created_at, status) "
                        + "VALUES (?, ?, ?, ?, ?, 'DRAFT')",
                id, creatorId, spaceId, noon(purchased), noon(uploaded));
        return id;
    }

    /** A product created [minute] minutes after midnight, so creation order is explicit. */
    private UUID insertProduct(UUID receiptId, String name, int minute) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO products (id, name, actual_storage_spot_id, product_type, shopping_receipt_id, created_at) "
                        + "VALUES (?, ?, ?, 'DAIRY', ?, ?)",
                id, name, storageSpotId, receiptId, Timestamp.valueOf(SEP_10.atStartOfDay().plusMinutes(minute)));
        return id;
    }

    private static Timestamp noon(LocalDate day) {
        return Timestamp.valueOf(day.atTime(12, 0));
    }
}
