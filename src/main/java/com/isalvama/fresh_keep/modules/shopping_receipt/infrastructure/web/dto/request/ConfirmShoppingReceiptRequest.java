package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.hibernate.validator.constraints.UUID;

import java.time.LocalDate;
import java.util.List;

public record ConfirmShoppingReceiptRequest(

        @UUID
        @NotNull
        @Schema(description = "Id of the draft ShoppingReceipt created by processNewShoppingReceipt for this same receiptImageId/spaceId/creator; finalized by this call")
        String shoppingReceiptId,

        @UUID
        @NotNull
        @Schema(description = "Must match the draft's own stored receiptImageId; not independently looked up")
        String receiptImageId,

        @NotNull
        @PastOrPresent
        @Schema(description = "Must not be in the future. If it differs from the draft's purchase date, every " +
                "non-manually-edited product's expirationDate is shifted by the same number of days")
        LocalDate shoppingDate,

        @NotBlank
        @Schema(description = "Store name")
        String storeName,

        @NotEmpty
        @Schema(description = "The full product list to persist as-is (no AI re-extraction)")
        List<ProductRequest> allProducts
) {
}
