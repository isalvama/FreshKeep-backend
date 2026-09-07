package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProductRequest(
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate expirationDate,

        @NotBlank
        @Size(max = 30)
        String productName,

        @NotNull
        UUID suggestedStorageSpotId,

        @NotBlank
        @Size(max = 30)
        String productType,

        @Positive
        BigDecimal priceAmount,

        @Size(max = 20)
        String currency
) {
}
