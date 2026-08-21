package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.request;

import com.isalvama.fresh_keep.shared.infrastructure.web.validation.EnumValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StorageSpotRequest(
        @NotBlank
        @Size (max= 30)
        String name,

        @NotBlank
        @EnumValue(enumClass = StorageSpotTypeRequest.class, message = "Type is an invalid StorageSpotType")
        String type
) {
}
