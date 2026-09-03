package com.isalvama.fresh_keep.shared.infrastructure.ai.dto;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductReviewFlag;

import java.util.List;

public record ReceiptExtractionToReview(
        List<ProductReviewFlag> flaggedProducts
) {
}
