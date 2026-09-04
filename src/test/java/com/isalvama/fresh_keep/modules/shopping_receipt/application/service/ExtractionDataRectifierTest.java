package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.product.infrastructure.product_categories_look_up.ProductCategoriesLookUpAdapter;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReceiptExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.RectifyExtractionDto;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExtractionDataRectifierTest {

    private final ExtractionDataRectifier rectifier = new ExtractionDataRectifier(new ProductCategoriesLookUpAdapter());

    private final Clock clockNow = Clock.fixed(Instant.parse("2026-09-01T10:00:00Z"), ZoneOffset.UTC);
    private final LocalDate today = LocalDate.now(clockNow);
    private final LocalDate tomorrow = today.plusDays(1);

    private final String storeName = "SuperMart";
    private final String fridgeId = "fridge-id";
    private final LocalDate milkSuggestedExpDate = LocalDate.of(2026, 9, 10);

    private final List<ProductExtraction> productExtractionList = List.of(
            new ProductExtraction(milkSuggestedExpDate, "Milk", fridgeId, "DAIRY", 2.5, "USD")
    );

    private final List<StorageSpotDto> storageSpotDtos = List.of(StorageSpotDto.create(fridgeId, "Fridge", "FRIDGE"));

    @Test
    void rectifyPurchaseDate_purchaseDateAndProductSuggestedDateShouldBeRectified() {
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(tomorrow, storeName, null, productExtractionList),
                storageSpotDtos,
                clockNow
        );

        ReceiptExtraction extractionResult = rectifier.rectify(dto);
        assertNotNull(extractionResult);

        assertEquals(extractionResult.purchaseDate(), tomorrow.minusDays(1));
        assertEquals(extractionResult.productExtractions().getFirst().expirationDate(), milkSuggestedExpDate.minusDays(1));
    }

    @Test
    void rectifyPurchaseDate_productSuggestedSuggestedStorageSpotShouldBeRectified() {
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(
                        LocalDate.now(clockNow).minusDays(1),
                        storeName,
                        null,
                        List.of(new ProductExtraction(milkSuggestedExpDate, "Milk", "invalid-id", "DAIRY", 2.5, "USD"))
                ),
                storageSpotDtos,
                clockNow
        );

        ReceiptExtraction extractionResult = rectifier.rectify(dto);
        assertNotNull(extractionResult);

        assertEquals(extractionResult.productExtractions().getFirst().suggestedStorageSpotId(), fridgeId);
    }

    @Test
    void rectify_leavesPurchaseDateAndExpirationDateUnchangedWhenPurchaseDateIsNotInTheFuture() {
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(today, storeName, null, productExtractionList),
                storageSpotDtos,
                clockNow
        );

        ReceiptExtraction extractionResult = rectifier.rectify(dto);

        assertEquals(today, extractionResult.purchaseDate());
        assertEquals(milkSuggestedExpDate, extractionResult.productExtractions().getFirst().expirationDate());
    }

    @Test
    void rectify_keepsExpirationDateNullWhenAiDidNotProvideOne() {
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(
                        tomorrow,
                        storeName,
                        null,
                        List.of(new ProductExtraction(null, "Milk", fridgeId, "DAIRY", 2.5, "USD"))
                ),
                storageSpotDtos,
                clockNow
        );

        ReceiptExtraction extractionResult = rectifier.rectify(dto);

        assertNull(extractionResult.productExtractions().getFirst().expirationDate());
    }

    @Test
    void rectify_keepsValidSuggestedStorageSpotIdUnchanged() {
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(today, storeName, null, productExtractionList),
                storageSpotDtos,
                clockNow
        );

        ReceiptExtraction extractionResult = rectifier.rectify(dto);

        assertEquals(fridgeId, extractionResult.productExtractions().getFirst().suggestedStorageSpotId());
    }

    @Test
    void rectify_setsStorageSpotIdToNullWhenProductTypeIsUnrecognized() {
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(
                        today,
                        storeName,
                        null,
                        List.of(new ProductExtraction(milkSuggestedExpDate, "Mystery item", "invalid-id", "NOT_A_REAL_TYPE", 2.5, "USD"))
                ),
                storageSpotDtos,
                clockNow
        );

        ReceiptExtraction extractionResult = rectifier.rectify(dto);

        assertNull(extractionResult.productExtractions().getFirst().suggestedStorageSpotId());
    }

    @Test
    void rectify_setsStorageSpotIdToNullWhenNoStorageSpotOfThePreferredTypeExists() {
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(
                        today,
                        storeName,
                        null,
                        List.of(new ProductExtraction(LocalDate.of(2027, 1, 1), "Ice pops", "invalid-id", "FROZEN_FOODS", 3.0, "USD"))
                ),
                storageSpotDtos, // only a FRIDGE spot, no FREEZER
                clockNow
        );

        ReceiptExtraction extractionResult = rectifier.rectify(dto);

        assertNull(extractionResult.productExtractions().getFirst().suggestedStorageSpotId());
    }

    @Test
    void rectify_processesEachProductInTheListIndependently() {
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(
                        tomorrow,
                        storeName,
                        null,
                        List.of(
                                new ProductExtraction(milkSuggestedExpDate, "Milk", fridgeId, "DAIRY", 2.5, "USD"),
                                new ProductExtraction(LocalDate.of(2026, 9, 20), "Bread", "invalid-id", "BAKERY", 1.5, "USD")
                        )
                ),
                storageSpotDtos,
                clockNow
        );

        ReceiptExtraction extractionResult = rectifier.rectify(dto);

        assertEquals(2, extractionResult.productExtractions().size());

        ProductExtraction milk = extractionResult.productExtractions().get(0);
        assertEquals(fridgeId, milk.suggestedStorageSpotId());
        assertEquals(milkSuggestedExpDate.minusDays(1), milk.expirationDate());

        ProductExtraction bread = extractionResult.productExtractions().get(1);
        assertNull(bread.suggestedStorageSpotId()); // BAKERY's preferred type is COUNTERTOP, not present in storageSpotDtos
        assertEquals(LocalDate.of(2026, 9, 19), bread.expirationDate());
    }

    @Test
    void rectify_preservesStoreNameAndErrorReason() {
        RectifyExtractionDto dto = new RectifyExtractionDto(
                new ReceiptExtraction(today, storeName, "some error", productExtractionList),
                storageSpotDtos,
                clockNow
        );

        ReceiptExtraction extractionResult = rectifier.rectify(dto);

        assertEquals(storeName, extractionResult.storeName());
        assertEquals("some error", extractionResult.errorReason());
    }
}
