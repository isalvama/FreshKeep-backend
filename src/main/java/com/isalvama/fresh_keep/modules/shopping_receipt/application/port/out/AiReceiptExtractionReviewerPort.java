package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReviewNewShoppingReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductReviewFlag;

import java.util.List;

public interface AiReceiptExtractionReviewerPort {
    List<ProductReviewFlag> review(ReviewNewShoppingReceiptDto dto);
}
