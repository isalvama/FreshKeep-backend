package com.isalvama.fresh_keep.modules.product.application.service;

import com.isalvama.fresh_keep.modules.product.application.port.UpdateProductUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.UpdateProductCommand;
import com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.response.UpdateProductResponse;

public class UpdateProductService implements UpdateProductUseCase {
    @Override
    public UpdateProductResponse execute(UpdateProductCommand command) {
        return null;
    }
}
