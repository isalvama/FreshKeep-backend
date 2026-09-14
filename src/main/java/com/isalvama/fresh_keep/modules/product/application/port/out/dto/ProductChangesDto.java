package com.isalvama.fresh_keep.modules.product.application.port.out.dto;

import com.isalvama.fresh_keep.modules.product.application.service.dto.ProductMove;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductChangesDto(
        String newStorageSpotId,
        String newStorageSpotName,
        String newStorageSpotType,
        LocalDateTime changedAt,
        LocalDate newExpirationDate
) {

    public static ProductChangesDto from (ProductMove dto, StorageSpotInfoDto storageSpotInfo){
        return new ProductChangesDto(
                dto.newStorageSpotId(),
                storageSpotInfo.name(),
                storageSpotInfo.type(),
                dto.changedAt(),
                dto.newExpirationDate()
                );
    }
}
