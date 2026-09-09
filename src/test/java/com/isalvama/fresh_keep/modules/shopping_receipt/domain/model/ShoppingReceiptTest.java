package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.InvalidShoppingReceiptException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ReceiptImageId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingReceiptTest {
    private static final UserId CREATOR_ID = UserId.create();
    private static final SpaceId SPACE_ID = SpaceId.create();
    private static final ReceiptImageId RECEIPT_IMAGE_ID = ReceiptImageId.create();
    private static final String STORE_NAME = "Mercadona";
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-01T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void create_generatesShoppingReceiptWithGeneratedId() {
        LocalDate purchaseDate = LocalDate.now(CLOCK);

        ShoppingReceipt shoppingReceipt = ShoppingReceipt.create(CREATOR_ID, SPACE_ID, RECEIPT_IMAGE_ID, purchaseDate, STORE_NAME, CLOCK);

        assertNotNull(shoppingReceipt);
        assertNotNull(shoppingReceipt.getId());
        assertEquals(CREATOR_ID, shoppingReceipt.getCreatorId());
        assertEquals(SPACE_ID, shoppingReceipt.getSpaceId());
        assertEquals(RECEIPT_IMAGE_ID, shoppingReceipt.getReceiptImageId());
        assertEquals(purchaseDate, shoppingReceipt.getPurchaseDate());
        assertEquals(STORE_NAME, shoppingReceipt.getStoreName());
    }

    @Test
    void create_generatesADifferentIdOnEachCall() {
        LocalDate purchaseDate = LocalDate.now(CLOCK);

        ShoppingReceipt first = ShoppingReceipt.create(CREATOR_ID, SPACE_ID, RECEIPT_IMAGE_ID, purchaseDate, STORE_NAME, CLOCK);
        ShoppingReceipt second = ShoppingReceipt.create(CREATOR_ID, SPACE_ID, RECEIPT_IMAGE_ID, purchaseDate, STORE_NAME, CLOCK);

        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void create_allowsNullStoreName() {
        LocalDate purchaseDate = LocalDate.now(CLOCK);

        assertDoesNotThrow(() -> ShoppingReceipt.create(CREATOR_ID, SPACE_ID, RECEIPT_IMAGE_ID, purchaseDate, null, CLOCK));
    }

    @Test
    void create_throwsInvalidShoppingReceiptExceptionWhenPurchaseDateIsAfterToday() {
        LocalDate futureDate = LocalDate.now(CLOCK).plusDays(1);

        Exception exception = assertThrows(InvalidShoppingReceiptException.class,
                () -> ShoppingReceipt.create(CREATOR_ID, SPACE_ID, RECEIPT_IMAGE_ID, futureDate, STORE_NAME, CLOCK));
        assertTrue(exception.getMessage().contains("purchase date cannot be later than the current date"));
    }

    @Test
    void create_throwsInvalidShoppingReceiptExceptionWhenRequiredParamsAreNull() {
        LocalDate purchaseDate = LocalDate.now(CLOCK);

        assertThrows(InvalidShoppingReceiptException.class,
                () -> ShoppingReceipt.create(null, SPACE_ID, RECEIPT_IMAGE_ID, purchaseDate, STORE_NAME, CLOCK));
        assertThrows(InvalidShoppingReceiptException.class,
                () -> ShoppingReceipt.create(CREATOR_ID, null, RECEIPT_IMAGE_ID, purchaseDate, STORE_NAME, CLOCK));
        assertThrows(InvalidShoppingReceiptException.class,
                () -> ShoppingReceipt.create(CREATOR_ID, SPACE_ID, null, purchaseDate, STORE_NAME, CLOCK));
        assertThrows(InvalidShoppingReceiptException.class,
                () -> ShoppingReceipt.create(CREATOR_ID, SPACE_ID, RECEIPT_IMAGE_ID, null, STORE_NAME, CLOCK));
    }

    @Test
    void reconstitute_generatesShoppingReceiptSuccessfully() {
        ShoppingReceiptId id = ShoppingReceiptId.create();
        LocalDate purchaseDate = LocalDate.now(CLOCK);

        ShoppingReceipt shoppingReceipt = ShoppingReceipt.reconstitute(id, CREATOR_ID, SPACE_ID, RECEIPT_IMAGE_ID, purchaseDate, STORE_NAME);

        assertNotNull(shoppingReceipt);
        assertEquals(id, shoppingReceipt.getId());
        assertEquals(CREATOR_ID, shoppingReceipt.getCreatorId());
        assertEquals(SPACE_ID, shoppingReceipt.getSpaceId());
        assertEquals(RECEIPT_IMAGE_ID, shoppingReceipt.getReceiptImageId());
        assertEquals(purchaseDate, shoppingReceipt.getPurchaseDate());
        assertEquals(STORE_NAME, shoppingReceipt.getStoreName());
    }

    @Test
    void reconstitute_allowsPurchaseDateInTheFuture() {
        ShoppingReceiptId id = ShoppingReceiptId.create();
        LocalDate futureDate = LocalDate.now(CLOCK).plusDays(1);

        assertDoesNotThrow(() -> ShoppingReceipt.reconstitute(id, CREATOR_ID, SPACE_ID, RECEIPT_IMAGE_ID, futureDate, STORE_NAME));
    }

    @Test
    void reconstitute_throwsInvalidShoppingReceiptExceptionWhenRequiredParamsAreNull() {
        ShoppingReceiptId id = ShoppingReceiptId.create();
        LocalDate purchaseDate = LocalDate.now(CLOCK);

        assertThrows(InvalidShoppingReceiptException.class,
                () -> ShoppingReceipt.reconstitute(null, CREATOR_ID, SPACE_ID, RECEIPT_IMAGE_ID, purchaseDate, STORE_NAME));
        assertThrows(InvalidShoppingReceiptException.class,
                () -> ShoppingReceipt.reconstitute(id, null, SPACE_ID, RECEIPT_IMAGE_ID, purchaseDate, STORE_NAME));
        assertThrows(InvalidShoppingReceiptException.class,
                () -> ShoppingReceipt.reconstitute(id, CREATOR_ID, null, RECEIPT_IMAGE_ID, purchaseDate, STORE_NAME));
        assertThrows(InvalidShoppingReceiptException.class,
                () -> ShoppingReceipt.reconstitute(id, CREATOR_ID, SPACE_ID, null, purchaseDate, STORE_NAME));
        assertThrows(InvalidShoppingReceiptException.class,
                () -> ShoppingReceipt.reconstitute(id, CREATOR_ID, SPACE_ID, RECEIPT_IMAGE_ID, null, STORE_NAME));
    }

    @Test
    void setters_updateMutableFields() {
        ShoppingReceipt shoppingReceipt = ShoppingReceipt.create(CREATOR_ID, SPACE_ID, RECEIPT_IMAGE_ID, LocalDate.now(CLOCK), STORE_NAME, CLOCK);
        ReceiptImageId newReceiptImageId = ReceiptImageId.create();
        LocalDate newPurchaseDate = LocalDate.now(CLOCK).minusDays(1);

        shoppingReceipt.setReceiptImageId(newReceiptImageId);
        shoppingReceipt.setPurchaseDate(newPurchaseDate);
        shoppingReceipt.setStoreName("Carrefour");

        assertEquals(newReceiptImageId, shoppingReceipt.getReceiptImageId());
        assertEquals(newPurchaseDate, shoppingReceipt.getPurchaseDate());
        assertEquals("Carrefour", shoppingReceipt.getStoreName());
    }
}
