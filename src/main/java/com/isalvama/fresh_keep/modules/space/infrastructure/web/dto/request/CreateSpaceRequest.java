package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record CreateSpaceRequest(

        @NotBlank
        @Size(max = 20)
        String spaceName,

        @NotBlank
        @Size(min = 1, max = 8)
        String emoji,

        @NotNull
        @NotEmpty
        @Valid
        List<StorageSpotRequest> storageSpots
) {
}
