package com.isalvama.fresh_keep.shared.infrastructure.ai.receipt_extraction_reviewer;

import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductChangesDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductMovedDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.StorageSpotInfoDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReviewNewShoppingReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import com.isalvama.fresh_keep.shared.infrastructure.ai.dto.ReceiptExtractionToReview;
import org.junit.jupiter.api.Test;
import org.springframework.ai.converter.BeanOutputConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReviewPromptBuilderTest {
    private static final String TEMPLATE = "template text: {format}, {shoppingDate}, {storageSpots}, {products}";

    private final ReviewPromptBuilder promptBuilder = new ReviewPromptBuilder();
    private final BeanOutputConverter<ReceiptExtractionToReview> converter = new BeanOutputConverter<>(ReceiptExtractionToReview.class);

    private final List<ProductExtraction> products = List.of(
            new ProductExtraction(LocalDate.of(2026, 9, 10), "Milk", "fridge-id", "DAIRY", BigDecimal.valueOf(2.5), "USD")
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

    @Test
    void build_rendersProductMovedPromptWithAllPlaceholders() {
        LocalDate shoppingDate = LocalDate.of(2026, 9, 1);
        LocalDateTime changedAt = LocalDateTime.of(2026, 9, 5, 14, 30);
        Clock clock = Clock.fixed(Instant.parse("2026-09-16T10:15:00Z"), ZoneOffset.UTC);
        ProductChangesDto productChange = new ProductChangesDto(
                "pantry-id", "Pantry", "PANTRY", changedAt, LocalDate.of(2026, 9, 12));
        ProductMovedDto dto = new ProductMovedDto(
                "Milk", "DAIRY", shoppingDate, List.of(productChange),
                StorageSpotInfoDto.create("fridge-id", "Fridge", "FRIDGE"),
                StorageSpotInfoDto.create("pantry-id", "Pantry", "PANTRY"),
                clock);
        BeanOutputConverter<LocalDate> localDateConverter = new BeanOutputConverter<>(LocalDate.class);
        String template = "product={productName}; type={productType}; shoppingDate={shoppingDate}; "
                + "changes={productChanges}; old={oldStorageSpot}; new={newStorageSpot}; "
                + "today={today}; format={format}";

        String result = promptBuilder.build(template, dto, localDateConverter);

        String expectedChanges = "- storage spot name: Pantry, new storage spot type: PANTRY, "
                + "change date: 2026-09-05T14:30, new expiration date set with the change: 2026-09-12";
        String expected = ("product=Milk; type=DAIRY; shoppingDate=2026-09-01; changes=%s; "
                + "old=Old Storage Spot\n- id: fridge-id, name: Fridge, type: FRIDGE; "
                + "new=New Storage Spot\n- id: pantry-id, name: Pantry, type: PANTRY; "
                + "today=2026-09-16T10:15; format=%s").formatted(expectedChanges, localDateConverter.getFormat());

        assertEquals(expected, result);
    }
}
