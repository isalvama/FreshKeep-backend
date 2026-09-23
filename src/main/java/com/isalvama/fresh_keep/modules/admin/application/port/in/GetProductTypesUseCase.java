package com.isalvama.fresh_keep.modules.admin.application.port.in;

import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductTypeCountDto;

import java.util.List;

public interface GetProductTypesUseCase {

    List<ProductTypeCountDto> execute();
}
