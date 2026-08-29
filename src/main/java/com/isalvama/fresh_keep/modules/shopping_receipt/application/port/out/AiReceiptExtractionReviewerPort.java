package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out;

import com.isalvama.fresh_keep.shared.infrastructure.ai.dto.ProductExtraction;

import java.util.List;

public interface AiReceiptExtractionReviewerPort {
    List<ProductExtraction> review (List<ProductExtraction> products);
}
