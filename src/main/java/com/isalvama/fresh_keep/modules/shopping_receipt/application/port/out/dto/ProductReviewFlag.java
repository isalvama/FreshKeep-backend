package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto;

public record ProductReviewFlag(
        ProductExtraction product,
        String reason
) {
}
