package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jdbc;

import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ProductQueryAdapter.class, ProductQueryResultSetExtractor.class})
class ProductQueryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private ProductQueryAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID spaceId;
    private UUID storageSpotId;
    private UUID shoppingReceiptId;

    @BeforeEach
    void setUp() {
        spaceId = UUID.randomUUID();
        storageSpotId = UUID.randomUUID();
        shoppingReceiptId = UUID.randomUUID();

        insertSpace(spaceId, "Kitchen", "🏠");
        insertStorageSpot(storageSpotId, "Fridge", "FRIDGE", spaceId);
        insertShoppingReceipt(shoppingReceiptId);
    }

    @Test
    void getSpaceProducts_returnsEmptyListWhenSpaceHasNoProducts() {
        List<ProductQueryDto> result = adapter.getSpaceProducts(spaceId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getSpaceProducts_returnsProductsSortedByExpirationDateAscending() {
        UUID yogurtId = UUID.randomUUID();
        UUID milkId = UUID.randomUUID();
        UUID breadId = UUID.randomUUID();

        insertProduct(yogurtId, "Yogurt", LocalDate.of(2026, 9, 20), storageSpotId, "DAIRY", shoppingReceiptId, BigDecimal.valueOf(2.0), "USD");
        insertProduct(milkId, "Milk", LocalDate.of(2026, 9, 10), storageSpotId, "DAIRY", shoppingReceiptId, BigDecimal.valueOf(1.5), "USD");
        insertProduct(breadId, "Bread", LocalDate.of(2026, 9, 15), storageSpotId, "BAKERY", shoppingReceiptId, BigDecimal.valueOf(1.0), "USD");

        List<ProductQueryDto> result = adapter.getSpaceProducts(spaceId);

        assertEquals(3, result.size());
        assertEquals(milkId, result.get(0).id());
        assertEquals(breadId, result.get(1).id());
        assertEquals(yogurtId, result.get(2).id());
    }

    @Test
    void getSpaceProducts_mapsEveryColumnCorrectly() {
        UUID productId = UUID.randomUUID();
        insertProduct(productId, "Milk", LocalDate.of(2026, 9, 20), storageSpotId, "DAIRY", shoppingReceiptId, BigDecimal.valueOf(1.5), "USD");

        List<ProductQueryDto> result = adapter.getSpaceProducts(spaceId);

        assertEquals(1, result.size());
        ProductQueryDto product = result.getFirst();
        assertEquals(productId, product.id());
        assertEquals("Milk", product.name());
        assertEquals(LocalDate.of(2026, 9, 20), product.expirationDate());
        assertEquals(storageSpotId, product.actualStorageSpotId());
        assertEquals("DAIRY", product.productType());
        assertEquals(shoppingReceiptId, product.shoppingReceiptId());
        assertEquals(0, BigDecimal.valueOf(1.5).compareTo(product.price()));
        assertEquals("USD", product.currency());
    }

    @Test
    void getSpaceProducts_doesNotReturnProductsFromAnotherSpace() {
        UUID otherSpaceId = UUID.randomUUID();
        UUID otherStorageSpotId = UUID.randomUUID();
        insertSpace(otherSpaceId, "Garage", "🚗");
        insertStorageSpot(otherStorageSpotId, "Shelf", "SHELF", otherSpaceId);

        insertProduct(UUID.randomUUID(), "Milk", LocalDate.of(2026, 9, 20), storageSpotId, "DAIRY", shoppingReceiptId, BigDecimal.valueOf(1.5), "USD");
        insertProduct(UUID.randomUUID(), "Motor Oil", LocalDate.of(2027, 1, 1), otherStorageSpotId, "OTHER", shoppingReceiptId, null, null);

        List<ProductQueryDto> result = adapter.getSpaceProducts(spaceId);

        assertEquals(1, result.size());
        assertEquals("Milk", result.getFirst().name());
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

    private void insertProduct(UUID id, String name, LocalDate expirationDate, UUID actualStorageSpotId,
                                String productType, UUID shoppingReceiptId, BigDecimal price, String currency) {
        jdbcTemplate.update(
                "INSERT INTO products (id, name, expiration_date, suggested_storage_spot_id, actual_storage_spot_id, product_type, shopping_receipt_id, price, currency) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, name, expirationDate, actualStorageSpotId, actualStorageSpotId, productType, shoppingReceiptId, price, currency
        );
    }
}
