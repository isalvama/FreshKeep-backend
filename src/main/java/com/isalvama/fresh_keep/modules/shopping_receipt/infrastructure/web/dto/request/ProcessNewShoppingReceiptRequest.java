package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UUID;
import org.springframework.web.multipart.MultipartFile;

public record ProcessNewShoppingReceiptRequest(

        @NotNull
        MultipartFile file,

        @NotBlank
        @UUID
        String spaceId
) {
}
