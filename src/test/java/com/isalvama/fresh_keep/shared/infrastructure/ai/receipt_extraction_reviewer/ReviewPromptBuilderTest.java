package com.isalvama.fresh_keep.shared.infrastructure.ai.receipt_extraction_reviewer;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReviewNewShoppingReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import com.isalvama.fresh_keep.shared.infrastructure.ai.dto.ReceiptExtractionToReview;
import org.junit.jupiter.api.Test;
import org.springframework.ai.converter.BeanOutputConverter;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReviewPromptBuilderTest {
    private static final String TEMPLATE = "template text: {format}, {shoppingDate}, {storageSpots}, {products}";

    private final ReviewPromptBuilder promptBuilder = new ReviewPromptBuilder();
    private final BeanOutputConverter<ReceiptExtractionToReview> converter = new BeanOutputConverter<>(ReceiptExtractionToReview.class);

    private final List<ProductExtraction> products = List.of(
            new ProductExtraction(LocalDate.of(2026, 9, 10), "Milk", "fridge-id", "DAIRY", 2.5, "USD")
    );

    @Test
    void build_rendersEveryPlaceholderWithTheExactExpectedValue() {
        ReviewNewShoppingReceiptDto dto = new ReviewNewShoppingReceiptDto(
                List.of(
                        StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE"),
                        StorageSpotDto.create("pantry-id", "Pantry", "PANTRY")
                ),
                products,
                LocalDate.of(2026, 9, 1)
        );

        String result = promptBuilder.build(TEMPLATE, dto, converter);

        String expected = ("template text: %s, 2026-09-01, " +
                "- id: fridge-id, name: Fridge, type: FRIDGE\n- id: pantry-id, name: Pantry, type: PANTRY, %s")
                .formatted(converter.getFormat(), products.toString());

        assertEquals(expected, result);
    }

    @Test
    void build_leavesNoPlaceholderTokenUnresolved() {
        ReviewNewShoppingReceiptDto dto = new ReviewNewShoppingReceiptDto(
                List.of(StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE")),
                products,
                LocalDate.of(2026, 9, 1)
        );

        String result = promptBuilder.build(TEMPLATE, dto, converter);

        assertFalse(result.contains("{format}"));
        assertFalse(result.contains("{shoppingDate}"));
        assertFalse(result.contains("{storageSpots}"));
        assertFalse(result.contains("{products}"));
    }

    @Test
    void build_joinsASingleStorageSpotWithoutStraySeparators() {
        ReviewNewShoppingReceiptDto dto = new ReviewNewShoppingReceiptDto(
                List.of(StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE")),
                products,
                LocalDate.of(2026, 9, 1)
        );

        String result = promptBuilder.build(TEMPLATE, dto, converter);

        assertTrue(result.contains(", - id: fridge-id, name: Fridge, type: FRIDGE, "));
    }
}
