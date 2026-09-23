package com.isalvama.fresh_keep.modules.admin.application.port.in;

import com.isalvama.fresh_keep.modules.admin.application.command.GetProductsCommand;
import com.isalvama.fresh_keep.modules.admin.domain.criteria.ProductSortType;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;

import java.util.List;

public interface GetProductsUseCase {

    List<ProductQueryDto> execute(GetProductsCommand command);
}
