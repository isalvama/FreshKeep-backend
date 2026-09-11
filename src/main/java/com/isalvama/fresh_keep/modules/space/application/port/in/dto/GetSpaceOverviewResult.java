package com.isalvama.fresh_keep.modules.space.application.port.in.dto;

import java.util.List;

public record GetSpaceOverviewResult(
        String id,
        String name,
        String emoji,
        List<StorageSpotResult> storageSpots,
        List<SpaceProductResult> productResults
) {
}
