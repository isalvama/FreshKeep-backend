package com.isalvama.fresh_keep.modules.space.application.port.out;

import com.isalvama.fresh_keep.modules.space.application.port.out.dto.SpaceProductDto;

import java.util.List;
import java.util.UUID;

public interface SpaceProductsLookUpPort {
    List<SpaceProductDto> getProductsFromSpaceId(UUID spaceId);
}
