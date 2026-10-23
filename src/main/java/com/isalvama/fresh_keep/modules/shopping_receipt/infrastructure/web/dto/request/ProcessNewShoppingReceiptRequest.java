package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record ProcessNewShoppingReceiptRequest(
        @NotNull
        @Schema(description = "The receipt image, required, must not be empty", type = "string", format = "binary")
        MultipartFile file,

        @NotBlank
        @Schema(description = "Language subtag only (e.g. 'es', not 'es-AR'), case-insensitive. An unrecognized " +
                "value is not an error; the backend falls back to English", example = "en")
        String language
) {
}
