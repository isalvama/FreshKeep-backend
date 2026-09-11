package com.isalvama.fresh_keep.modules.product.application.port.out;

import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;

import java.util.List;
import java.util.UUID;

public interface ProductQueryPort {

    List<ProductQueryDto> getSpaceProducts(UUID spaceId);
}
