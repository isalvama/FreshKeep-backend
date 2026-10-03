package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record CreateSpaceRequest(

        @NotBlank
        @Size(max = 30)
        @Schema(description = "Space name, max 30 characters, must contain at least one letter", example = "Kitchen")
        String spaceName,

        @NotBlank
        @Size(min = 1, max = 8)
        @Schema(description = "1-8 characters, must consist only of actual emoji codepoints", example = "🏠")
        String emoji,

        @NotNull
        @NotEmpty
        @Valid
        @Schema(description = "At least one storage spot. Two storage spots with the same name AND type are rejected")
        List<StorageSpotRequest> storageSpots
) {
}
