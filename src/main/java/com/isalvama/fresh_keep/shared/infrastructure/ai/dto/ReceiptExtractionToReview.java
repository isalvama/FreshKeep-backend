package com.isalvama.fresh_keep.shared.infrastructure.ai.dto;

import java.util.List;

public record ReceiptExtractionToReview(
        List<ProductReviewFlag> flaggedProducts
) {
}
