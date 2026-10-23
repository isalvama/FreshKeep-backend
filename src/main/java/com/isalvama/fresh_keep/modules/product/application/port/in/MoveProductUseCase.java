package com.isalvama.fresh_keep.modules.product.application.port.in;

import com.isalvama.fresh_keep.modules.product.application.port.in.command.MoveProductCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.result.MoveProductResult;

public interface MoveProductUseCase {

    MoveProductResult execute(MoveProductCommand command);
}
