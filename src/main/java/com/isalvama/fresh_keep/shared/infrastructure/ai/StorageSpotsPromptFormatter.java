package com.isalvama.fresh_keep.shared.infrastructure.ai;

import java.util.List;
import java.util.stream.Collectors;

public final class StorageSpotsPromptFormatter {

    private StorageSpotsPromptFormatter() {
    }

    public static String format(List<StorageSpotData> storageSpots) {
        return storageSpots.stream().map(StorageSpotsPromptFormatter::buildString)
                .collect(Collectors.joining("\n"));
    }

    public static String formatWithTitle(StorageSpotData storageSpot, String title) {
        return title + "\n" + buildString(storageSpot);
    }

    private static String buildString(StorageSpotData data){
        return "- id: %s, name: %s, type: %s".formatted(data.id(), data.name(), data.type());
    }

    public record StorageSpotData(
            String id,
            String name,
            String type
    ){}
}
