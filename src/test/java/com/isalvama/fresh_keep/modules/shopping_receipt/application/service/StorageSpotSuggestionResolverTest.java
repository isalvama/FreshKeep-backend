package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.product.infrastructure.product_categories_look_up.ProductCategoriesLookUpAdapter;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StorageSpotSuggestionResolverTest {

    private final StorageSpotSuggestionResolver resolver = new StorageSpotSuggestionResolver(new ProductCategoriesLookUpAdapter());

    private final String fridgeId = "fridge-id";
    private final LocalDate milkSuggestedExpDate = LocalDate.of(2026, 9, 10);

    private final List<StorageSpotDto> storageSpotDtos = List.of(StorageSpotDto.create(fridgeId, "Fridge", "FRIDGE"));

    @Test
    void resolve_keepsValidSuggestedStorageSpotIdUnchanged() {
        List<ProductExtraction> products = List.of(
                new ProductExtraction(milkSuggestedExpDate, "Milk", fridgeId, "DAIRY", BigDecimal.valueOf(2.5), "USD")
        );

        List<ProductExtraction> result = resolver.resolve(products, storageSpotDtos);

        assertEquals(fridgeId, result.getFirst().suggestedStorageSpotId());
    }

    @Test
    void resolve_replacesInvalidStorageSpotIdWithFallbackBasedOnProductType() {
        List<ProductExtraction> products = List.of(
                new ProductExtraction(milkSuggestedExpDate, "Milk", "invalid-id", "DAIRY", BigDecimal.valueOf(2.5), "USD")
        );

        List<ProductExtraction> result = resolver.resolve(products, storageSpotDtos);

        assertEquals(fridgeId, result.getFirst().suggestedStorageSpotId());
    }

    @Test
    void resolve_setsStorageSpotIdToNullWhenProductTypeIsUnrecognized() {
        List<ProductExtraction> products = List.of(
                new ProductExtraction(milkSuggestedExpDate, "Mystery item", "invalid-id", "NOT_A_REAL_TYPE", BigDecimal.valueOf(2.5), "USD")
        );

        List<ProductExtraction> result = resolver.resolve(products, storageSpotDtos);

        assertNull(result.getFirst().suggestedStorageSpotId());
    }

    @Test
    void resolve_setsStorageSpotIdToNullWhenNoStorageSpotOfThePreferredTypeExists() {
        List<ProductExtraction> products = List.of(
                new ProductExtraction(LocalDate.of(2027, 1, 1), "Ice pops", "invalid-id", "FROZEN_FOODS", BigDecimal.valueOf(3.0), "USD")
        );

        List<ProductExtraction> result = resolver.resolve(products, storageSpotDtos); // only a FRIDGE spot, no FREEZER

        assertNull(result.getFirst().suggestedStorageSpotId());
    }

    @Test
    void resolve_processesEachProductInTheListIndependently() {
        List<ProductExtraction> products = List.of(
                new ProductExtraction(milkSuggestedExpDate, "Milk", fridgeId, "DAIRY", BigDecimal.valueOf(2.5), "USD"),
                new ProductExtraction(LocalDate.of(2026, 9, 20), "Bread", "invalid-id", "BAKERY", BigDecimal.valueOf(1.5), "USD")
        );

        List<ProductExtraction> result = resolver.resolve(products, storageSpotDtos);

        assertEquals(2, result.size());

        ProductExtraction milk = result.get(0);
        assertEquals(fridgeId, milk.suggestedStorageSpotId());

        ProductExtraction bread = result.get(1);
        assertNull(bread.suggestedStorageSpotId()); // BAKERY's preferred type is COUNTERTOP, not present in storageSpotDtos
    }

    @Test
    void resolve_doesNotTouchExpirationDateOrOtherProductFields() {
        List<ProductExtraction> products = List.of(
                new ProductExtraction(milkSuggestedExpDate, "Milk", "invalid-id", "DAIRY", BigDecimal.valueOf(2.5), "USD")
        );

        List<ProductExtraction> result = resolver.resolve(products, storageSpotDtos);

        ProductExtraction resolved = result.getFirst();
        assertEquals(milkSuggestedExpDate, resolved.expirationDate());
        assertEquals("Milk", resolved.productName());
        assertEquals("DAIRY", resolved.productType());
        assertEquals(BigDecimal.valueOf(2.5), resolved.priceAmount());
        assertEquals("USD", resolved.currency());
    }
}
