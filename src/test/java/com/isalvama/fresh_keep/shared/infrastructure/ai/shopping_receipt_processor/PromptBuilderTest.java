package com.isalvama.fresh_keep.shared.infrastructure.ai.shopping_receipt_processor;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProcessNewShoppingReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReceiptExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import org.junit.jupiter.api.Test;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromptBuilderTest {
    private static final String TEMPLATE = "template text: {format}, {productTypes}, {moneyCurrencies}, {storageSpots}, {today}";

    private final PromptBuilder promptBuilder = new PromptBuilder();
    private final BeanOutputConverter<ReceiptExtraction> converter = new BeanOutputConverter<>(ReceiptExtraction.class);
    private final MockMultipartFile file = new MockMultipartFile(
            "file",
            "receipt.jpg",
            "image/jpeg",
            "fake-image-content".getBytes()
    );
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-01T10:00:00Z"), ZoneOffset.UTC);

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
                List.of("USD", "EUR")
        );

        String result = promptBuilder.build(TEMPLATE, dto, converter);

        String expectedToday = LocalDateTime.now(clock).toString();
        String expected = ("template text: %s, DAIRY, FRUITS, PANTRY, USD, EUR, " +
                "- id: fridge-id, name: Fridge, type: FRIDGE\n- id: pantry-id, name: Pantry, type: PANTRY, %s")
                .formatted(converter.getFormat(), expectedToday);

        assertEquals("", result);
    }

    @Test
    void build_leavesNoPlaceholderTokenUnresolved() {
        ProcessNewShoppingReceiptDto dto = new ProcessNewShoppingReceiptDto(
                file,
                List.of(StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE")),
                clock,
                List.of("DAIRY"),
                List.of("USD")
        );

        String result = promptBuilder.build(TEMPLATE, dto, converter);

        assertFalse(result.contains("{format}"));
        assertFalse(result.contains("{productTypes}"));
        assertFalse(result.contains("{moneyCurrencies}"));
        assertFalse(result.contains("{storageSpots}"));
        assertFalse(result.contains("{today}"));
    }

    @Test
    void build_joinsSingleElementListsWithoutStraySeparators() {
        ProcessNewShoppingReceiptDto dto = new ProcessNewShoppingReceiptDto(
                file,
                List.of(StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE")),
                clock,
                List.of("DAIRY"),
                List.of("USD")
        );

        String result = promptBuilder.build(TEMPLATE, dto, converter);

        assertTrue(result.contains(", DAIRY, "));
        assertTrue(result.contains(", USD, "));
        assertTrue(result.contains(", - id: fridge-id, name: Fridge, type: FRIDGE,"));
    }
}
