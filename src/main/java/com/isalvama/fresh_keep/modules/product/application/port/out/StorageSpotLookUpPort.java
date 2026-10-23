package com.isalvama.fresh_keep.modules.product.application.port.out;

import com.isalvama.fresh_keep.modules.product.application.port.out.dto.StorageSpotInfoDto;

import java.util.List;
import java.util.Set;

public interface StorageSpotLookUpPort {
    List<StorageSpotInfoDto> findByIds(Set<String> storageSpotIds);
}
