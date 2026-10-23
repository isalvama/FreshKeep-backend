package com.isalvama.fresh_keep.shared.infrastructure.ai.shopping_receipt_processor;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProcessNewShoppingReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReceiptExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReprocessShoppingReceiptWithFlaggedProductsDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import com.isalvama.fresh_keep.shared.infrastructure.ai.ProductExtractionsPromptFormatter;
import org.junit.jupiter.api.Test;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromptBuilderTest {
    private static final String TEMPLATE = "template text: {format}, {productTypes}, {moneyCurrencies}, {storageSpots}, {today}, {language}";

    private final PromptBuilder promptBuilder = new PromptBuilder();
    private final BeanOutputConverter<ReceiptExtraction> converter = new BeanOutputConverter<>(ReceiptExtraction.class);
    private final MockMultipartFile file = new MockMultipartFile(
            "file",
            "receipt.jpg",
            "image/jpeg",
            "fake-image-content".getBytes()
    );
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-01T10:00:00Z"), ZoneOffset.UTC);
    private final String language = "English";

    @Test
    void build_rendersEveryPlaceholderWithTheExactExpectedValue() {
        ProcessNewShoppingReceiptDto dto = new ProcessNewShoppingReceiptDto(
                file,
                List.of(
                        StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE"),
                        StorageSpotDto.create("pantry-id", "Pantry", "PANTRY"),
                        StorageSpotDto.create("freezer-id", "Freezer", "FREEZER")

                ),
                clock,
                List.of("DAIRY", "FRUITS", "PANTRY"),
                List.of("USD", "EUR"),
                language
        );

        String result = promptBuilder.build(TEMPLATE, dto, converter);

        String expectedToday = LocalDateTime.now(clock).toString();
        String expected = ("template text: %s, DAIRY, FRUITS, PANTRY, USD, EUR, " +
                "- id: fridge-id, name: Fridge, type: FRIDGE\n- id: pantry-id, name: Pantry, type: PANTRY\n" +
                "- id: freezer-id, name: Freezer, type: FREEZER, %s, %s")
                .formatted(converter.getFormat(), expectedToday, language);

        assertEquals(expected, result);
    }

    @Test
    void build_leavesNoPlaceholderTokenUnresolved() {
        ProcessNewShoppingReceiptDto dto = new ProcessNewShoppingReceiptDto(
                file,
                List.of(StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE")),
                clock,
                List.of("DAIRY"),
                List.of("USD"),
                language
        );

        String result = promptBuilder.build(TEMPLATE, dto, converter);

        assertFalse(result.contains("{format}"));
        assertFalse(result.contains("{productTypes}"));
        assertFalse(result.contains("{moneyCurrencies}"));
        assertFalse(result.contains("{storageSpots}"));
        assertFalse(result.contains("{today}"));
        assertFalse(result.contains("{language}"));
    }

    @Test
    void build_joinsSingleElementListsWithoutStraySeparators() {
        ProcessNewShoppingReceiptDto dto = new ProcessNewShoppingReceiptDto(
                file,
                List.of(StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE")),
                clock,
                List.of("DAIRY"),
                List.of("USD"),
                language
        );

        String result = promptBuilder.build(TEMPLATE, dto, converter);

        assertTrue(result.contains(", DAIRY, "));
        assertTrue(result.contains(", USD, "));
        assertTrue(result.contains(", - id: fridge-id, name: Fridge, type: FRIDGE,"));
    }

    private static final String REPROCESS_TEMPLATE =
            "template text: {format}, {productTypes}, {moneyCurrencies}, {storageSpots}, {today}, {language}, {purchaseDate}, {storeName}, {allProducts}, {productsToReview}";

    private final ProductExtraction milk = new ProductExtraction(LocalDate.of(2026, 9, 10), "Milk", "fridge-id", "DAIRY", BigDecimal.valueOf(1.5), "USD");
    private final ProductExtraction yogurt = new ProductExtraction(LocalDate.of(2026, 9, 12), "Yogurt", "fridge-id", "DAIRY", BigDecimal.valueOf(2.0), "USD");

    @Test
    void build_reprocess_rendersEveryPlaceholderWithTheExactExpectedValue() {
        ReprocessShoppingReceiptWithFlaggedProductsDto dto = new ReprocessShoppingReceiptWithFlaggedProductsDto(
                "fake-image-content".getBytes(),
                "image/jpeg",
                LocalDate.of(2026, 9, 1),
                "SuperMart",
                List.of(milk),
                List.of(yogurt),
                List.of(StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE")),
                clock,
                List.of("DAIRY"),
                List.of("USD"),
                language
        );

        String result = promptBuilder.build(REPROCESS_TEMPLATE, dto, converter);

        String expectedToday = LocalDateTime.now(clock).toString();
        String expected = ("template text: %s, DAIRY, USD, - id: fridge-id, name: Fridge, type: FRIDGE, %s, %s, " +
                "2026-09-01, SuperMart, %s, %s")
                .formatted(converter.getFormat(), expectedToday, language,
                        ProductExtractionsPromptFormatter.format(List.of(milk)),
                        ProductExtractionsPromptFormatter.format(List.of(yogurt)));

        assertEquals(expected, result);
    }

    @Test
    void build_reprocess_leavesNoPlaceholderTokenUnresolved() {
        ReprocessShoppingReceiptWithFlaggedProductsDto dto = new ReprocessShoppingReceiptWithFlaggedProductsDto(
                "fake-image-content".getBytes(),
                "image/jpeg",
                LocalDate.of(2026, 9, 1),
                "SuperMart",
                List.of(milk),
                List.of(yogurt),
                List.of(StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE")),
                clock,
                List.of("DAIRY"),
                List.of("USD"),
                language
        );

        String result = promptBuilder.build(REPROCESS_TEMPLATE, dto, converter);

        assertFalse(result.contains("{format}"));
        assertFalse(result.contains("{productTypes}"));
        assertFalse(result.contains("{moneyCurrencies}"));
        assertFalse(result.contains("{storageSpots}"));
        assertFalse(result.contains("{today}"));
        assertFalse(result.contains("{language}"));
        assertFalse(result.contains("{purchaseDate}"));
        assertFalse(result.contains("{storeName}"));
        assertFalse(result.contains("{allProducts}"));
        assertFalse(result.contains("{productsToReview}"));
    }

    @Test
    void build_reprocess_rendersEmptyStringWhenNoProductsToReview() {
        ReprocessShoppingReceiptWithFlaggedProductsDto dto = new ReprocessShoppingReceiptWithFlaggedProductsDto(
                "fake-image-content".getBytes(),
                "image/jpeg",
                LocalDate.of(2026, 9, 1),
                "SuperMart",
                List.of(milk),
                List.of(),
                List.of(StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE")),
                clock,
                List.of("DAIRY"),
                List.of("USD"),
                language
        );

        String result = promptBuilder.build(REPROCESS_TEMPLATE, dto, converter);

        assertFalse(result.contains("{productsToReview}"));
        assertTrue(result.endsWith(ProductExtractionsPromptFormatter.format(List.of(milk)) + ", "));
    }
}
