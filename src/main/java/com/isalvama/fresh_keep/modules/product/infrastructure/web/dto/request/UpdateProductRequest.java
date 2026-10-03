package com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.request;

import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Currency;
import com.isalvama.fresh_keep.shared.infrastructure.web.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateProductRequest (

        @Size(max= 30)
        @Pattern(regexp = ".*\\S.*")
        @Schema(description = "Optional. Max 30 characters, must contain at least one letter", example = "Milk")
        String name,

        @Schema(description = "Optional new expiration date")
        LocalDate expirationDate,

        @Size(max= 30)
        @Pattern(regexp = ".*\\S.*")
        @EnumValue(enumClass = ProductType.class, message = "productType is an invalid ProductType")
        @Schema(description = "Optional. Must be a valid ProductType constant name, validated as a bean-validation constraint (@EnumValue)", example = "DAIRY")
        String productType,

        @Positive
        @Schema(description = "Optional price amount, positive if present")
        BigDecimal amount,

        @Size(max= 20)
        @Pattern(regexp = ".*\\S.*")
        @EnumValue(enumClass = Currency.class, message = "currency is an invalid Currency")
        @Schema(description = "Optional. Must be a valid currency constant name, validated as a bean-validation constraint (@EnumValue)", example = "USD")
        String currency
) {
}
