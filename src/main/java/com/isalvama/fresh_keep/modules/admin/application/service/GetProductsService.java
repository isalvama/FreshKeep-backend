package com.isalvama.fresh_keep.modules.admin.application.service;

import com.isalvama.fresh_keep.modules.admin.application.command.GetProductsCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.in.GetProductsUseCase;
import com.isalvama.fresh_keep.modules.admin.domain.value_object.Pagination;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductQueryPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetProductsService implements GetProductsUseCase {

    private final ProductQueryPort productQueryPort;

    @Override
    @Transactional(readOnly = true)
    public List<ProductQueryDto> execute(GetProductsCommand command) {

        Pagination pagination = Pagination.fromPage(command.page(), command.size());

        ProductType productType = command.productType() == null
                ? null
                : ProductType.getValueOf(command.productType());

        return productQueryPort.getAllProducts(command.sort(), pagination.offset(), pagination.limit(), productType);
    }
}
