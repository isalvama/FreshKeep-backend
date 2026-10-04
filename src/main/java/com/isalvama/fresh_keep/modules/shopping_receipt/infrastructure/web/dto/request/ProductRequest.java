package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Currency;
import com.isalvama.fresh_keep.shared.infrastructure.web.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProductRequest(
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "Required")
        LocalDate expirationDate,

        @NotBlank
        @Size(max = 30)
        @Schema(description = "Required, max 30 characters, must contain at least one letter (checked at the domain layer, not bean-validated)")
        String productName,

        @NotNull
        @Schema(description = "Required")
        UUID suggestedStorageSpotId,

        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = ".*\\S.*")
        @EnumValue(enumClass = ProductType.class, message = "productType is an invalid ProductType")
        @Schema(description = "Required. Must be a valid ProductType constant name, validated as a bean-validation constraint (@EnumValue)", example = "DAIRY")
        String productType,

        @Positive
        @Schema(description = "Optional, must be positive if present")
        BigDecimal priceAmount,

        @Size(max = 20)
        @Pattern(regexp = ".*\\S.*")
        @EnumValue(enumClass = Currency.class, message = "currency is an invalid Currency")
        @Schema(description = "Optional. Must be a valid currency constant name, validated as a bean-validation constraint (@EnumValue)", example = "USD")
        String currency,

        @NotNull
        @Schema(description = "Required on every product. true when the user hand-edited this product's expiration " +
                "date; exempts it from purchase-date-shift rectification in confirm. reprocess currently ignores " +
                "its value but still requires it present")
        Boolean manuallyEditedExpirationDate
) {
}
