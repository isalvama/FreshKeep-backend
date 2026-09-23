package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jdbc;

import com.isalvama.fresh_keep.modules.admin.domain.criteria.ProductSortType;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductTypeCountDto;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
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

    @Test
    void getAllProducts_sortsByRequestedSortType() {
        UUID appleId = UUID.randomUUID();
        UUID milkId = UUID.randomUUID();
        UUID breadId = UUID.randomUUID();

        insertProduct(appleId, "Apple", LocalDate.of(2026, 9, 20), storageSpotId, "FRUITS", shoppingReceiptId, BigDecimal.valueOf(3.0), "USD");
        insertProduct(milkId, "Milk", LocalDate.of(2026, 9, 10), storageSpotId, "DAIRY", shoppingReceiptId, BigDecimal.valueOf(1.5), "USD");
        insertProduct(breadId, "Bread", LocalDate.of(2026, 9, 15), storageSpotId, "BAKERY", shoppingReceiptId, BigDecimal.valueOf(1.0), "USD");

        List<ProductQueryDto> result = adapter.getAllProducts(ProductSortType.PRICE_DESC, 0, 10, null);

        assertEquals(List.of(appleId, milkId, breadId), result.stream().map(ProductQueryDto::id).toList());
    }

    @Test
    void getAllProducts_doesNotReturnDeletedProducts() {
        UUID activeProductId = UUID.randomUUID();
        UUID deletedProductId = UUID.randomUUID();
        insertProduct(activeProductId, "Milk", LocalDate.of(2026, 9, 20), storageSpotId, "DAIRY", shoppingReceiptId, null, null);
        insertProduct(deletedProductId, "Bread", LocalDate.of(2026, 9, 15), storageSpotId, "BAKERY", shoppingReceiptId, null, null);
        jdbcTemplate.update("UPDATE products SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?", deletedProductId);

        List<ProductQueryDto> result = adapter.getAllProducts(ProductSortType.NAME_ASC, 0, 10, null);

        assertEquals(List.of(activeProductId), result.stream().map(ProductQueryDto::id).toList());
    }

    @Test
    void getAllProducts_filtersByProductType() {
        UUID dairyProductId = UUID.randomUUID();
        UUID bakeryProductId = UUID.randomUUID();
        insertProduct(dairyProductId, "Milk", LocalDate.of(2026, 9, 20), storageSpotId, "DAIRY", shoppingReceiptId, null, null);
        insertProduct(bakeryProductId, "Bread", LocalDate.of(2026, 9, 15), storageSpotId, "BAKERY", shoppingReceiptId, null, null);

        List<ProductQueryDto> result = adapter.getAllProducts(ProductSortType.NAME_ASC, 0, 10, ProductType.DAIRY);

        assertEquals(List.of(dairyProductId), result.stream().map(ProductQueryDto::id).toList());
    }

    @Test
    void getAllProducts_appliesLimitAndOffsetAfterSorting() {
        UUID appleId = UUID.randomUUID();
        UUID breadId = UUID.randomUUID();
        UUID milkId = UUID.randomUUID();
        insertProduct(milkId, "Milk", LocalDate.of(2026, 9, 20), storageSpotId, "DAIRY", shoppingReceiptId, null, null);
        insertProduct(breadId, "Bread", LocalDate.of(2026, 9, 15), storageSpotId, "BAKERY", shoppingReceiptId, null, null);
        insertProduct(appleId, "Apple", LocalDate.of(2026, 9, 10), storageSpotId, "FRUITS", shoppingReceiptId, null, null);

        List<ProductQueryDto> result = adapter.getAllProducts(ProductSortType.NAME_ASC, 1, 1, null);

        assertEquals(List.of(breadId), result.stream().map(ProductQueryDto::id).toList());
    }

    @Test
    void getProductTypesByCount_returnsTypesSortedDescendingByProductCount() {
        insertProduct(UUID.randomUUID(), "Apple", LocalDate.of(2026, 9, 20), storageSpotId, "FRUITS", shoppingReceiptId, null, null);
        insertProduct(UUID.randomUUID(), "Pear", LocalDate.of(2026, 9, 21), storageSpotId, "FRUITS", shoppingReceiptId, null, null);
        insertProduct(UUID.randomUUID(), "Banana", LocalDate.of(2026, 9, 22), storageSpotId, "FRUITS", shoppingReceiptId, null, null);
        insertProduct(UUID.randomUUID(), "Milk", LocalDate.of(2026, 9, 10), storageSpotId, "DAIRY", shoppingReceiptId, null, null);
        insertProduct(UUID.randomUUID(), "Bread", LocalDate.of(2026, 9, 15), storageSpotId, "BAKERY", shoppingReceiptId, null, null);
        insertProduct(UUID.randomUUID(), "Cake", LocalDate.of(2026, 9, 16), storageSpotId, "BAKERY", shoppingReceiptId, null, null);

        List<ProductTypeCountDto> result = adapter.getProductTypesByCount();

        assertEquals(List.of("FRUITS", "BAKERY", "DAIRY"), result.stream().map(ProductTypeCountDto::productType).toList());
        assertEquals(List.of(3L, 2L, 1L), result.stream().map(ProductTypeCountDto::productCount).toList());
    }

    @Test
    void getProductTypesByCount_ignoresDeletedProducts() {
        UUID activeProductId = UUID.randomUUID();
        UUID deletedProductId = UUID.randomUUID();
        insertProduct(activeProductId, "Milk", LocalDate.of(2026, 9, 20), storageSpotId, "DAIRY", shoppingReceiptId, null, null);
        insertProduct(deletedProductId, "Bread", LocalDate.of(2026, 9, 15), storageSpotId, "BAKERY", shoppingReceiptId, null, null);
        jdbcTemplate.update("UPDATE products SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?", deletedProductId);

        List<ProductTypeCountDto> result = adapter.getProductTypesByCount();

        assertEquals(List.of("DAIRY"), result.stream().map(ProductTypeCountDto::productType).toList());
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

    private void insertProduct(UUID id, String name, LocalDate expirationDate, UUID actualStorageSpotId,
                                String productType, UUID shoppingReceiptId, BigDecimal price, String currency) {
        jdbcTemplate.update(
                "INSERT INTO products (id, name, expiration_date, suggested_storage_spot_id, actual_storage_spot_id, product_type, shopping_receipt_id, price, currency) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, name, expirationDate, actualStorageSpotId, actualStorageSpotId, productType, shoppingReceiptId, price, currency
        );
    }
}
