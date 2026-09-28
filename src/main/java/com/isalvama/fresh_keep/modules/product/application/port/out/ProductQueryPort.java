package com.isalvama.fresh_keep.modules.product.application.port.out;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.DailyMetricDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.GetAllProductsDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductDetailDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductTypeCountDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductQueryPort {

    List<ProductQueryDto> getSpaceProducts(UUID spaceId);

    List<ProductQueryDto> getAllProducts(GetAllProductsDto dto);

    Optional<ProductDetailDto> getProductById(UUID productId);

    List<ProductTypeCountDto> getProductTypesByCount();

    List<DailyMetricDto> getProducts(LocalDate from, LocalDate to, UUID spaceId, UUID creatorId);

}
