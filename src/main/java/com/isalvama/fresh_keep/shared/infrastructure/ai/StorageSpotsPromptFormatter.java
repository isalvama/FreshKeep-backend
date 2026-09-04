package com.isalvama.fresh_keep.shared.infrastructure.ai;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;

import java.util.List;
import java.util.stream.Collectors;

public final class StorageSpotsPromptFormatter {

    private StorageSpotsPromptFormatter() {
    }

    public static String format(List<StorageSpotDto> storageSpots) {
        return storageSpots.stream()
                .map(s -> "- id: %s, name: %s, type: %s".formatted(s.id(), s.name(), s.type()))
                .collect(Collectors.joining("\n"));
    }
}
