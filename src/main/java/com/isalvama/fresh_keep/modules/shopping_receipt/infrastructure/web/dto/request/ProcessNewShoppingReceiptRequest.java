package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record ProcessNewShoppingReceiptRequest(
        @NotNull
        MultipartFile file
) {
}
