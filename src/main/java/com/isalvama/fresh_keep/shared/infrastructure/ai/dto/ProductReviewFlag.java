package com.isalvama.fresh_keep.shared.infrastructure.ai.dto;

public record ProductReviewFlag(
        ProductExtraction product,
        String reason
) {
}
