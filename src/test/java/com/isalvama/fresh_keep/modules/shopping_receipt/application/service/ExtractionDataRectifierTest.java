package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.ConfirmReceiptToRectifyDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.ProcessReceiptToRectifyDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.RectifiedReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ReceiptImageId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExtractionDataRectifierTest {

    private final ExtractionDataRectifier rectifier = new ExtractionDataRectifier();
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-01T10:00:00Z"), ZoneOffset.UTC);
    private final LocalDate today = LocalDate.now(clock);
    private final ProductExtraction milk = new ProductExtraction(
            LocalDate.of(2026, 9, 10), "Milk", "fridge-id", "DAIRY", BigDecimal.valueOf(2.5), "USD"
    );

    @Test
    void processRectification_clampsFuturePurchaseDateAndShiftsExpirationDates() {
        RectifiedReceiptDto result = rectifier.rectifyPurchaseDate(
                new ProcessReceiptToRectifyDto(today.plusDays(1), clock, List.of(milk))
        );

        assertEquals(today, result.purchaseDate());
        assertEquals(milk.expirationDate().minusDays(1), result.productExtractions().getFirst().expirationDate());
    }

    @Test
    void processRectification_leavesValidDatesUnchanged() {
        RectifiedReceiptDto result = rectifier.rectifyPurchaseDate(
                new ProcessReceiptToRectifyDto(today, clock, List.of(milk))
        );

        assertEquals(today, result.purchaseDate());
        assertEquals(milk.expirationDate(), result.productExtractions().getFirst().expirationDate());
    }

    @Test
    void processRectification_preservesNullExpirationDates() {
        ProductExtraction productWithoutExpiration = new ProductExtraction(
                null, "Milk", "fridge-id", "DAIRY", BigDecimal.valueOf(2.5), "USD"
        );

        RectifiedReceiptDto result = rectifier.rectifyPurchaseDate(
                new ProcessReceiptToRectifyDto(today.plusDays(1), clock, List.of(productWithoutExpiration))
        );

        assertNull(result.productExtractions().getFirst().expirationDate());
    }

    @Test
    void confirmationRectification_shiftsDatesWhenTheShoppingDateMovesForward() {
        LocalDate oldDate = today.minusDays(2);
        ShoppingReceipt receipt = ShoppingReceipt.createDraft(
                UserId.create(), SpaceId.create(), ReceiptImageId.create(), oldDate, "Store", clock
        );

        RectifiedReceiptDto result = rectifier.rectifyPurchaseDate(
                new ConfirmReceiptToRectifyDto(oldDate, today, clock, List.of(milk))
        );

        assertEquals(today, result.purchaseDate());
        assertEquals(milk.expirationDate().plusDays(2), result.productExtractions().getFirst().expirationDate());
        assertEquals(receipt.getPurchaseDate(), oldDate);
    }

    @Test
    void confirmationRectification_shiftsDatesWhenTheShoppingDateMovesBackward() {
        LocalDate editedDate = today.minusDays(1);

        RectifiedReceiptDto result = rectifier.rectifyPurchaseDate(
                new ConfirmReceiptToRectifyDto(today, editedDate, clock, List.of(milk))
        );

        assertEquals(editedDate, result.purchaseDate());
        assertEquals(milk.expirationDate().minusDays(1), result.productExtractions().getFirst().expirationDate());
    }

    @Test
    void confirmationRectification_keepsOriginalDateWhenEditedDateIsInTheFuture() {
        LocalDate oldDate = today.minusDays(1);

        RectifiedReceiptDto result = rectifier.rectifyPurchaseDate(
                new ConfirmReceiptToRectifyDto(oldDate, today.plusDays(1), clock, List.of(milk))
        );

        assertEquals(oldDate, result.purchaseDate());
        assertEquals(milk.expirationDate(), result.productExtractions().getFirst().expirationDate());
    }
}
