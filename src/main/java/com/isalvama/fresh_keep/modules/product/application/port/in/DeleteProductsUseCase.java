package com.isalvama.fresh_keep.modules.product.application.port.in;

import com.isalvama.fresh_keep.modules.product.application.port.in.command.DeleteProductsCommand;

public interface DeleteProductsUseCase {

    void execute(DeleteProductsCommand command);
}
