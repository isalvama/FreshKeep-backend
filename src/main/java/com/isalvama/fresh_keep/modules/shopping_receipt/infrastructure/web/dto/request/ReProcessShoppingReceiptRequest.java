package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "Id of the draft ShoppingReceipt created by processNewShoppingReceipt for this same receiptImageId/spaceId/creator; finalized by this call")
        String shoppingReceiptId,

        @UUID
        @NotNull
        @Schema(description = "Id of the ReceiptImage created via processNewShoppingReceipt; must belong to the same draft")
        String receiptImageId,

        @NotNull
        @PastOrPresent
        @Schema(description = "Must not be in the future (evaluated against the server's system clock)")
        LocalDate shoppingDate,

        @NotBlank
        @Schema(description = "Store name")
        String storeName,

        @NotEmpty
        @Schema(description = "The products the user flagged for re-extraction")
        List<ProductRequest> flaggedProducts,

        @NotEmpty
        @Schema(description = "The full current product list (flagged + unflagged), used as context for the AI")
        List<ProductRequest> allProducts,

        @NotBlank
        @Schema(description = "Language subtag only (e.g. 'es', not 'es-AR'), case-insensitive. An unrecognized " +
                "value is not an error; the backend falls back to English", example = "en")
        String language
) {
}
