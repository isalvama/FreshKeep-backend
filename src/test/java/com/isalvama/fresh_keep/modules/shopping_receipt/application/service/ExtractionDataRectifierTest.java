package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReceiptExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.RectifyExtractionDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExtractionDataRectifierTest {

    private final ExtractionDataRectifier rectifier = new ExtractionDataRectifier();

    private final Clock clockNow = Clock.fixed(Instant.parse("2026-09-01T10:00:00Z"), ZoneOffset.UTC);
    private final LocalDate today = LocalDate.now(clockNow);
    private final LocalDate tomorrow = today.plusDays(1);

    private final String storeName = "SuperMart";
    private final String fridgeId = "fridge-id";
    private final LocalDate milkSuggestedExpDate = LocalDate.of(2026, 9, 10);

    private final List<ProductExtraction> productExtractionList = List.of(
            new ProductExtraction(milkSuggestedExpDate, "Milk", fridgeId, "DAIRY", BigDecimal.valueOf(2.5), "USD")
    );

    private final List<StorageSpotDto> storageSpotDtos = List.of(StorageSpotDto.create(fridgeId, "Fridge", "FRIDGE"));

    @Test
    void rectifyPurchaseDate_clampsAFuturePurchaseDateToToday() {
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(tomorrow, storeName, null, productExtractionList),
                storageSpotDtos,
                clockNow
        );

        ReceiptExtraction result = rectifier.rectifyPurchaseDate(dto);

        assertEquals(today, result.purchaseDate());
    }

    @Test
    void rectifyPurchaseDate_shiftsEveryProductsExpirationDateByTheSameCorrectionWhenPurchaseDateIsClamped() {
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(tomorrow, storeName, null, productExtractionList),
                storageSpotDtos,
                clockNow
        );

        ReceiptExtraction result = rectifier.rectifyPurchaseDate(dto);

        assertEquals(milkSuggestedExpDate.minusDays(1), result.productExtractions().getFirst().expirationDate());
    }

    @Test
    void rectifyPurchaseDate_leavesPurchaseDateAndExpirationDateUnchangedWhenPurchaseDateIsNotInTheFuture() {
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(today, storeName, null, productExtractionList),
                storageSpotDtos,
                clockNow
        );

        ReceiptExtraction result = rectifier.rectifyPurchaseDate(dto);

        assertEquals(today, result.purchaseDate());
        assertEquals(milkSuggestedExpDate, result.productExtractions().getFirst().expirationDate());
    }

    @Test
    void rectifyPurchaseDate_keepsExpirationDateNullWhenAiDidNotProvideOne() {
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(
                        tomorrow,
                        storeName,
                        null,
                        List.of(new ProductExtraction(null, "Milk", fridgeId, "DAIRY", BigDecimal.valueOf(2.5), "USD"))
                ),
                storageSpotDtos,
                clockNow
        );

        ReceiptExtraction result = rectifier.rectifyPurchaseDate(dto);

        assertNull(result.productExtractions().getFirst().expirationDate());
    }

    @Test
    void rectifyPurchaseDate_doesNotTouchSuggestedStorageSpotId() {
        // Storage-spot fallback resolution is StorageSpotSuggestionResolver's responsibility now, not this class's.
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(
                        tomorrow,
                        storeName,
                        null,
                        List.of(new ProductExtraction(milkSuggestedExpDate, "Milk", "invalid-id", "DAIRY", BigDecimal.valueOf(2.5), "USD"))
                ),
                storageSpotDtos,
                clockNow
        );

        ReceiptExtraction result = rectifier.rectifyPurchaseDate(dto);

        assertEquals("invalid-id", result.productExtractions().getFirst().suggestedStorageSpotId());
    }

    @Test
    void rectifyPurchaseDate_processesEachProductInTheListIndependently() {
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(
                        tomorrow,
                        storeName,
                        null,
                        List.of(
                                new ProductExtraction(milkSuggestedExpDate, "Milk", fridgeId, "DAIRY", BigDecimal.valueOf(2.5), "USD"),
                                new ProductExtraction(LocalDate.of(2026, 9, 20), "Bread", fridgeId, "BAKERY", BigDecimal.valueOf(1.5), "USD")
                        )
                ),
                storageSpotDtos,
                clockNow
        );

        ReceiptExtraction result = rectifier.rectifyPurchaseDate(dto);

        assertEquals(2, result.productExtractions().size());
        assertEquals(milkSuggestedExpDate.minusDays(1), result.productExtractions().get(0).expirationDate());
        assertEquals(LocalDate.of(2026, 9, 19), result.productExtractions().get(1).expirationDate());
    }

    @Test
    void rectifyPurchaseDate_preservesStoreNameAndErrorReason() {
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(today, storeName, "some error", productExtractionList),
                storageSpotDtos,
                clockNow
        );

        ReceiptExtraction result = rectifier.rectifyPurchaseDate(dto);

        assertEquals(storeName, result.storeName());
        assertEquals("some error", result.errorReason());
    }
}
