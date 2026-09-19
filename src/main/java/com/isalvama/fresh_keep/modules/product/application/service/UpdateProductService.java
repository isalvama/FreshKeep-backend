package com.isalvama.fresh_keep.modules.product.application.service;

import com.isalvama.fresh_keep.modules.product.application.port.UpdateProductUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.UpdateProductCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.result.UpdateProductResult;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductRepositoryPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.SpaceParticipancyLookUpPort;
import com.isalvama.fresh_keep.modules.product.domain.exception.NonExistentProductException;
import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Amount;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Currency;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductName;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateProductService implements UpdateProductUseCase {
    private final ProductRepositoryPort productRepositoryPort;
    private final SpaceParticipancyLookUpPort spaceParticipancyLookUpPort;

    @Override
    public UpdateProductResult execute(UpdateProductCommand command) {
        Product product = productRepositoryPort.findById(ProductId.of(command.productId()))
                .orElseThrow(() -> new NonExistentProductException("Product with id " + command.productId() + " does not exist."));

        boolean isUserParticipant = spaceParticipancyLookUpPort.isParticipant(command.userId().toString(), product.getActualStorageSpotId().toString());

        if (!isUserParticipant){
            throw new SpaceNotAccessibleException("User with id " + command.userId() + " is not a participant of the storage spot with id " + product.getActualStorageSpotId().toString());
        }

        product.update(
                command.name() == null ? null : ProductName.from(command.name()),
                command.expirationDate(),
                command.productType() == null ? null : ProductType.getValueOf(command.productType()),
                command.amount() == null ? null : Amount.of(command.amount()),
                command.currency() == null ? null : Currency.getValueOf(command.currency())
        );

        productRepositoryPort.save(product);

        return new UpdateProductResult(
                product.getId().toString(),
                product.getName().value(),
                product.getExpirationDate(),
                product.getProductType().name(),
                product.getPrice() == null ? null : product.getPrice().getAmountValue(),
                product.getPrice() == null ? null : product.getPrice().getCurrencyName()
        );
    }

}
