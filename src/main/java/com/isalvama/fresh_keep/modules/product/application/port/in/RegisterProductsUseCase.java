package com.isalvama.fresh_keep.modules.product.application.port.in;

import com.isalvama.fresh_keep.modules.product.application.port.in.command.RegisterProductsCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.result.RegisteredProductResult;

import java.util.List;

public interface RegisterProductsUseCase {
    List<RegisteredProductResult> execute (RegisterProductsCommand command);
}
