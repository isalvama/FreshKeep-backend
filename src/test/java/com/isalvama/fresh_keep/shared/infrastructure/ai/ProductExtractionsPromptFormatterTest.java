package com.isalvama.fresh_keep.shared.infrastructure.ai;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductExtractionsPromptFormatterTest {
    private static final ProductExtraction MILK = new ProductExtraction(
            LocalDate.of(2026, 9, 10), "Milk", "fridge-id", "DAIRY", BigDecimal.valueOf(1.5), "USD"
    );
    private static final ProductExtraction YOGURT = new ProductExtraction(
            LocalDate.of(2026, 9, 12), "Yogurt", "fridge-id", "DAIRY", BigDecimal.valueOf(2.0), "USD"
    );

    @Test
    void format_returnsFormattedLineForASingleProduct() {
        String result = ProductExtractionsPromptFormatter.format(List.of(MILK));

        assertEquals(
                "- product name: Milk, expiration date: 2026-09-10, suggested storage spot id: fridge-id, product type: DAIRY, price amount: 1.5, currency: USD",
                result
        );
    }

    @Test
    void format_joinsMultipleProductsWithNewline() {
        String result = ProductExtractionsPromptFormatter.format(List.of(MILK, YOGURT));

        String[] lines = result.split("\n");
        assertEquals(2, lines.length);
        assertTrue(lines[0].contains("Milk"));
        assertTrue(lines[1].contains("Yogurt"));
    }

    @Test
    void format_returnsEmptyStringForEmptyList() {
        assertEquals("", ProductExtractionsPromptFormatter.format(List.of()));
    }

    @Test
    void format_rendersNullFieldsAsNotAvailable() {
        ProductExtraction incomplete = new ProductExtraction(null, "Milk", null, "DAIRY", null, null);

        String result = ProductExtractionsPromptFormatter.format(List.of(incomplete));

        assertTrue(result.contains("expiration date: N/A"));
        assertTrue(result.contains("suggested storage spot id: N/A"));
        assertTrue(result.contains("price amount: N/A"));
        assertTrue(result.contains("currency: N/A"));
        assertFalse(result.contains("null"));
    }
}
