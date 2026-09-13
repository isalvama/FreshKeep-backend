package com.isalvama.fresh_keep.modules.product.application.service;

import com.isalvama.fresh_keep.modules.product.application.port.in.DeleteProductUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.DeleteProductCommand;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductRepositoryPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.SpaceParticipancyLookUpPort;
import com.isalvama.fresh_keep.modules.product.domain.exception.NonExistentProductException;
import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteProductService implements DeleteProductUseCase {
    private final ProductRepositoryPort productRepositoryPort;
    private final SpaceParticipancyLookUpPort spaceParticipancyLookUpPort;

    @Override
    public void execute(DeleteProductCommand command) {

        Product product = productRepositoryPort.findById(ProductId.from(command.productId()))
                .orElseThrow(() -> new NonExistentProductException("Receipt Image with id " + command.productId() + " does not exist."));

        boolean isUserParticipant = spaceParticipancyLookUpPort.isParticipant(command.userId(), product.getActualStorageSpotId().toString());

        if (isUserParticipant){
            throw new SpaceNotAccessibleException("User with id " + command.userId() + " is not a participant of the Space with id " + command);
        }

        productRepositoryPort.delete(product);
    }
}
