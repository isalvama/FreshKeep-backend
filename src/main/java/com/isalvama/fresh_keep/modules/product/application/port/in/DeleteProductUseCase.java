package com.isalvama.fresh_keep.modules.product.application.port.in;

import com.isalvama.fresh_keep.modules.product.application.port.in.command.DeleteProductCommand;
import org.springframework.stereotype.Service;

@Service
public interface DeleteProductUseCase {
    void execute (DeleteProductCommand command);
}
