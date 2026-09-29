package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jdbc;

import com.isalvama.fresh_keep.modules.admin.domain.criteria.ProductSortType;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.GetAllProductsDto;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jdbc.QueryAppender;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductTypeCountDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductDetailDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.DailyMetricDto;
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
@Import({ProductQueryAdapter.class, ProductQueryResultSetExtractor.class, QueryAppender.class})
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

        List<ProductQueryDto> result = adapter.getAllProducts(new GetAllProductsDto(ProductSortType.PRICE_DESC, 0, 10, null, null, null));

        assertEquals(List.of(appleId, milkId, breadId), result.stream().map(ProductQueryDto::id).toList());
    }

    @Test
    void getAllProducts_doesNotReturnDeletedProducts() {
        UUID activeProductId = UUID.randomUUID();
        UUID deletedProductId = UUID.randomUUID();
        insertProduct(activeProductId, "Milk", LocalDate.of(2026, 9, 20), storageSpotId, "DAIRY", shoppingReceiptId, null, null);
        insertProduct(deletedProductId, "Bread", LocalDate.of(2026, 9, 15), storageSpotId, "BAKERY", shoppingReceiptId, null, null);
        jdbcTemplate.update("UPDATE products SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?", deletedProductId);

        List<ProductQueryDto> result = adapter.getAllProducts(new GetAllProductsDto(ProductSortType.NAME_ASC, 0, 10, null, null, null));

        assertEquals(List.of(activeProductId), result.stream().map(ProductQueryDto::id).toList());
    }

    @Test
    void getAllProducts_filtersByProductType() {
        UUID dairyProductId = UUID.randomUUID();
        UUID bakeryProductId = UUID.randomUUID();
        insertProduct(dairyProductId, "Milk", LocalDate.of(2026, 9, 20), storageSpotId, "DAIRY", shoppingReceiptId, null, null);
        insertProduct(bakeryProductId, "Bread", LocalDate.of(2026, 9, 15), storageSpotId, "BAKERY", shoppingReceiptId, null, null);

        List<ProductQueryDto> result = adapter.getAllProducts(new GetAllProductsDto(ProductSortType.NAME_ASC, 0, 10, ProductType.DAIRY, null, null));

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

        List<ProductQueryDto> result = adapter.getAllProducts(new GetAllProductsDto(ProductSortType.NAME_ASC, 1, 1, null, null, null));

        assertEquals(List.of(breadId), result.stream().map(ProductQueryDto::id).toList());
    }

    @Test
    void getAllProducts_filtersByCreatorAndShoppingReceipt() {
        UUID creatorId = UUID.randomUUID();
        UUID otherCreatorId = UUID.randomUUID();
        UUID matchingId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, ?)",
                UUID.randomUUID(), "creator@email.com", "hash");
        UUID accountId = jdbcTemplate.queryForObject("SELECT id FROM accounts WHERE email = ?", UUID.class, "creator@email.com");
        jdbcTemplate.update("INSERT INTO users (id, account_id, email) VALUES (?, ?, ?)", creatorId, accountId, "creator@email.com");
        UUID otherAccountId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, ?)", otherAccountId, "other@email.com", "hash");
        jdbcTemplate.update("INSERT INTO users (id, account_id, email) VALUES (?, ?, ?)", otherCreatorId, otherAccountId, "other@email.com");
        UUID matchingReceiptId = UUID.randomUUID();
        UUID otherReceiptId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO shopping_receipts (id, creator_id, status) VALUES (?, ?, 'DRAFT')", matchingReceiptId, creatorId);
        jdbcTemplate.update("INSERT INTO shopping_receipts (id, creator_id, status) VALUES (?, ?, 'DRAFT')", otherReceiptId, otherCreatorId);
        insertProduct(matchingId, "Milk", LocalDate.of(2026, 9, 20), storageSpotId, "DAIRY", matchingReceiptId, null, null);
        insertProduct(otherId, "Bread", LocalDate.of(2026, 9, 15), storageSpotId, "BAKERY", otherReceiptId, null, null);

        List<ProductQueryDto> result = adapter.getAllProducts(new GetAllProductsDto(
                ProductSortType.NAME_ASC, 0, 10, null, creatorId.toString(), matchingReceiptId.toString()));

        assertEquals(List.of(matchingId), result.stream().map(ProductQueryDto::id).toList());
    }

    @Test
    void getProductById_returnsDetailsAndEmptyForMissingProduct() {
        UUID productId = UUID.randomUUID();
        insertProduct(productId, "Milk", LocalDate.of(2026, 9, 20), storageSpotId,
                "DAIRY", shoppingReceiptId, BigDecimal.valueOf(1.5), "USD");

        ProductDetailDto result = adapter.getProductById(productId).orElseThrow();

        assertEquals(productId, result.id());
        assertEquals("Milk", result.name());
        assertEquals("FRIDGE", result.storageSpotIdType());
        assertTrue(adapter.getProductById(UUID.randomUUID()).isEmpty());
    }

    @Test
    void getProducts_returnsDailyCountsWithDateAndResourceFilters() {
        UUID creatorId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, ?)", accountId, "metric@email.com", "hash");
        jdbcTemplate.update("INSERT INTO users (id, account_id, email) VALUES (?, ?, ?)", creatorId, accountId, "metric@email.com");
        UUID receiptId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO shopping_receipts (id, creator_id, space_id, status, created_at) VALUES (?, ?, ?, 'DRAFT', ?)",
                receiptId, creatorId, spaceId, java.sql.Timestamp.valueOf("2026-03-10 10:00:00"));
        UUID firstProductId = UUID.randomUUID();
        UUID secondProductId = UUID.randomUUID();
        insertProduct(firstProductId, "Milk", LocalDate.of(2026, 9, 20), storageSpotId, "DAIRY", receiptId, null, null);
        insertProduct(secondProductId, "Bread", LocalDate.of(2026, 9, 15), storageSpotId, "BAKERY", receiptId, null, null);
        jdbcTemplate.update("UPDATE products SET created_at = ? WHERE id = ?", java.sql.Timestamp.valueOf("2026-03-10 11:00:00"), firstProductId);
        jdbcTemplate.update("UPDATE products SET created_at = ? WHERE id = ?", java.sql.Timestamp.valueOf("2026-03-11 11:00:00"), secondProductId);

        List<DailyMetricDto> result = adapter.getProducts(
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 11), spaceId, creatorId);

        assertEquals(List.of(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 11)),
                result.stream().map(DailyMetricDto::date).toList());
        assertEquals(List.of(1L, 1L), result.stream().map(DailyMetricDto::count).toList());
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
