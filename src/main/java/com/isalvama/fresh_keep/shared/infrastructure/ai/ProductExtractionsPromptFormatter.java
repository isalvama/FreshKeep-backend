package com.isalvama.fresh_keep.shared.infrastructure.ai;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProductExtractionsPromptFormatter {

    private ProductExtractionsPromptFormatter() {
    }

    public static String format(List<ProductExtraction> productExtractions) {
        return productExtractions.stream()
                .map(s -> {
                    return "- product name: %s, expiration date: %s, suggested storage spot id: %s, product type: %s, price amount: %s, currency: %s"
                            .formatted(toString(s.productName()), toString(s.expirationDate()), toString(s.suggestedStorageSpotId()), toString(s.productType()), toString(s.priceAmount()), toString(s.currency()));
                })
                .collect(Collectors.joining("\n"));
    }
    private static <T> String toString(T object){
        return Objects.toString(object, "N/A");
    }
}