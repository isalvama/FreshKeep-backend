package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.hibernate.validator.constraints.UUID;

import java.time.LocalDate;
import java.util.List;

public record ReProcessShoppingReceiptRequest (
        @UUID
        @NotNull
        String receiptImageId,

        @NotNull
        @PastOrPresent
        LocalDate shoppingDate,

        @NotBlank
        String storeName,

        @NotEmpty
        List<ProductRequest> flaggedProducts,

        @NotEmpty
        List<ProductRequest> allProducts
) {
}
