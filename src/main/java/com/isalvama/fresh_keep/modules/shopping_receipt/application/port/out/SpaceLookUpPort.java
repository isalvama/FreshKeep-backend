package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.GetStorageSpotsDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;

import java.util.List;

public interface SpaceLookUpPort {
    List<StorageSpotDto> getStorageSpotsBySpaceIdAndParticipantId (GetStorageSpotsDto dto);
}
