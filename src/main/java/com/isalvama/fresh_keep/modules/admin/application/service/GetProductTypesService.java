package com.isalvama.fresh_keep.modules.admin.application.service;

import com.isalvama.fresh_keep.modules.admin.application.port.in.GetProductTypesUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductQueryPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductTypeCountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetProductTypesService implements GetProductTypesUseCase {

    private final ProductQueryPort productQueryPort;

    @Override
    @Transactional(readOnly = true)
    public List<ProductTypeCountDto> execute() {
        return productQueryPort.getProductTypesByCount();
    }
}
