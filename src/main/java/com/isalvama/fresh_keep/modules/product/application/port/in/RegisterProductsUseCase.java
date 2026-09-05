package com.isalvama.fresh_keep.modules.product.application.port.in;

import com.isalvama.fresh_keep.modules.product.application.port.in.command.RegisterProductsCommand;

public interface RegisterProductsUseCase {
    void execute (RegisterProductsCommand command);
}
