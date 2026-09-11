package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response;

import com.isalvama.fresh_keep.modules.space.application.port.in.dto.SpaceProductResult;

import java.util.List;

public record SpaceOverviewResponse(
        String id,
        String name,
        String emoji,
        List<StorageSpotResponse> storageSpots,
        List<SpaceProductResponse> productResults
) {
}
