package com.isalvama.fresh_keep.shared.infrastructure.ai;

import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductChangesDto;

import java.util.List;
import java.util.stream.Collectors;

public class ProductChangesPromptFormatter {
    private ProductChangesPromptFormatter() {
    }

    public static String format(List<ProductChangesDto> productChangesDtos) {
        return productChangesDtos.stream()
                .map(pc -> "- storage spot name: %s, new storage spot type: %s, change date: %s, new expiration date set with the change: %s".formatted(pc.newStorageSpotName(), pc.newStorageSpotType(), pc.changedAt().toString(), pc.newExpirationDate()))
                .collect(Collectors.joining("\n"));
    }
}
