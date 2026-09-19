package com.isalvama.fresh_keep.modules.product.application.port;

import com.isalvama.fresh_keep.modules.product.application.port.in.command.UpdateProductCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.result.UpdateProductResult;

public interface UpdateProductUseCase {
    UpdateProductResult execute (UpdateProductCommand command);
}
