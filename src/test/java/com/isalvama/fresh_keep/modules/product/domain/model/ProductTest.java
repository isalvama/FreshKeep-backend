package com.isalvama.fresh_keep.modules.product.domain.model;

import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidProductException;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Money;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Amount;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductName;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {
    private static final ProductName NAME = ProductName.from("Tomatoes");
    private static final LocalDate EXPIRATION_DATE = LocalDate.now().plusDays(7);
    private static final StorageSpotId SUGGESTED_STORAGE_SPOT_ID = StorageSpotId.create();
    private static final ProductType PRODUCT_TYPE = ProductType.VEGETABLES;
    private static final ShoppingReceiptId SHOPPING_RECEIPT_ID = ShoppingReceiptId.create();
    private static final Money PRICE = Money.from(BigDecimal.valueOf(2.5), "EUR");

    @Test
    void create_generatesProductWithGeneratedId() {
        Product product = Product.create(NAME, EXPIRATION_DATE, SUGGESTED_STORAGE_SPOT_ID, PRODUCT_TYPE, SHOPPING_RECEIPT_ID, PRICE);

        assertNotNull(product);
        assertNotNull(product.getId());
        assertEquals(NAME, product.getName());
        assertEquals(EXPIRATION_DATE, product.getExpirationDate());
        assertEquals(SUGGESTED_STORAGE_SPOT_ID, product.getSuggestedStorageSpotId());
        assertEquals(SUGGESTED_STORAGE_SPOT_ID, product.getActualStorageSpotId());
        assertEquals(PRODUCT_TYPE, product.getProductType());
        assertEquals(SHOPPING_RECEIPT_ID, product.getShoppingReceiptId());
        assertEquals(PRICE, product.getPrice());
    }

    @Test
    void create_generatesADifferentIdOnEachCall() {
        Product first = Product.create(NAME, EXPIRATION_DATE, SUGGESTED_STORAGE_SPOT_ID, PRODUCT_TYPE, SHOPPING_RECEIPT_ID, PRICE);
        Product second = Product.create(NAME, EXPIRATION_DATE, SUGGESTED_STORAGE_SPOT_ID, PRODUCT_TYPE, SHOPPING_RECEIPT_ID, PRICE);

        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void reconstitute_preservesActualStorageSpotWhenItDiffersFromSuggestedSpot() {
        StorageSpotId actualStorageSpotId = StorageSpotId.create();

        Product product = Product.reconstitute(
                ProductId.create(), NAME, EXPIRATION_DATE, SUGGESTED_STORAGE_SPOT_ID,
                actualStorageSpotId, PRODUCT_TYPE, SHOPPING_RECEIPT_ID, PRICE);

        assertEquals(SUGGESTED_STORAGE_SPOT_ID, product.getSuggestedStorageSpotId());
        assertEquals(actualStorageSpotId, product.getActualStorageSpotId());
    }

    @Test
    void create_allowsNullPrice() {
        assertDoesNotThrow(() -> Product.create(NAME, EXPIRATION_DATE, SUGGESTED_STORAGE_SPOT_ID, PRODUCT_TYPE, SHOPPING_RECEIPT_ID, null));
    }

    @Test
    void create_throwsInvalidProductExceptionWhenRequiredParamsAreNull() {
        assertThrows(InvalidProductException.class,
                () -> Product.create(null, EXPIRATION_DATE, SUGGESTED_STORAGE_SPOT_ID, PRODUCT_TYPE, SHOPPING_RECEIPT_ID, PRICE));
        assertThrows(InvalidProductException.class,
                () -> Product.create(NAME, null, SUGGESTED_STORAGE_SPOT_ID, PRODUCT_TYPE, SHOPPING_RECEIPT_ID, PRICE));
        assertThrows(InvalidProductException.class,
                () -> Product.create(NAME, EXPIRATION_DATE, null, PRODUCT_TYPE, SHOPPING_RECEIPT_ID, PRICE));
        assertThrows(InvalidProductException.class,
                () -> Product.create(NAME, EXPIRATION_DATE, SUGGESTED_STORAGE_SPOT_ID, null, SHOPPING_RECEIPT_ID, PRICE));
        assertThrows(InvalidProductException.class,
                () -> Product.create(NAME, EXPIRATION_DATE, SUGGESTED_STORAGE_SPOT_ID, PRODUCT_TYPE, null, PRICE));
    }

    @Test
    void update_combinesPartialPriceChangesBeforeReplacingThePrice() {
        Product product = Product.create(NAME, EXPIRATION_DATE, SUGGESTED_STORAGE_SPOT_ID, PRODUCT_TYPE, SHOPPING_RECEIPT_ID, PRICE);

        product.update(null, null, null, Amount.of(BigDecimal.valueOf(3.75)), null);

        assertEquals(Money.from(BigDecimal.valueOf(3.75), "EUR"), product.getPrice());
    }

    @Test
    void setters_updateMutableFields() {
        Product product = Product.create(NAME, EXPIRATION_DATE, SUGGESTED_STORAGE_SPOT_ID, PRODUCT_TYPE, SHOPPING_RECEIPT_ID, PRICE);
        StorageSpotId actualStorageSpotId = StorageSpotId.create();
        LocalDate newExpirationDate = EXPIRATION_DATE.plusDays(1);

        product.updateStorageSpot(actualStorageSpotId, newExpirationDate);

        assertEquals(actualStorageSpotId, product.getActualStorageSpotId());
        assertEquals(newExpirationDate, product.getExpirationDate());
    }
}
