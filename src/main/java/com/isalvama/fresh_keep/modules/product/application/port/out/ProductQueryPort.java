package com.isalvama.fresh_keep.modules.product.application.port.out;

import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductTypeCountDto;
import com.isalvama.fresh_keep.modules.admin.domain.criteria.ProductSortType;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;

import java.util.List;
import java.util.UUID;

public interface ProductQueryPort {

    List<ProductQueryDto> getSpaceProducts(UUID spaceId);

    List<ProductQueryDto> getAllProducts(ProductSortType sortType, Integer offset, Integer limit, ProductType productType);

    List<ProductTypeCountDto> getProductTypesByCount();
}
