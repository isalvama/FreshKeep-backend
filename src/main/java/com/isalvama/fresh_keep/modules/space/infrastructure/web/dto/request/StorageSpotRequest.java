package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.request;

import com.isalvama.fresh_keep.shared.infrastructure.web.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StorageSpotRequest(
        @NotBlank
        @Size (max= 30)
        @Schema(description = "Storage spot name, max 30 characters, must contain at least one letter", example = "Main Shelf")
        String name,

        @NotBlank
        @EnumValue(enumClass = StorageSpotTypeRequest.class, message = "Type is an invalid StorageSpotType")
        @Schema(description = "One of FRIDGE, FREEZER, PANTRY, FRUIT_BOWL, WINE_CELLAR, COUNTERTOP, SHELF", example = "SHELF")
        String type
) {
}
