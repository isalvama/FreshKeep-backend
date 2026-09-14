package com.isalvama.fresh_keep.modules.product.application.service;

import com.isalvama.fresh_keep.modules.product.application.port.in.DeleteProductsUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.DeleteProductsCommand;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductRepositoryPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.SpaceParticipancyLookUpPort;
import com.isalvama.fresh_keep.modules.product.domain.exception.NonExistentProductException;
import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeleteProductsService implements DeleteProductsUseCase {
    private final ProductRepositoryPort productRepositoryPort;
    private final SpaceParticipancyLookUpPort spaceParticipancyLookUpPort;
    private final Clock clock;

    @Override
    @Transactional
    public void execute(DeleteProductsCommand command) {

        List<ProductId> productIds = command.productsIds().stream().map(ProductId::of).toList();
        List<Product> products = productRepositoryPort.findAllById(productIds);

        List<ProductId> missingIds = products.stream().map(Product::getId).filter(id -> !productIds.contains(id)).toList();
        if (!missingIds.isEmpty()){
            throw new NonExistentProductException("Products with id " + String.join(", ", missingIds.toString()) + " not found.");
        }

        Set<String> storageSpotsIds = products.stream().map(p -> p.getId().toString()).collect(Collectors.toSet());
        Set<String> accessibleSpotsIds = spaceParticipancyLookUpPort.filterAccessible(command.userId().toString(), storageSpotsIds);

        List<Product> notAccessibleProducts = products.stream().filter(p -> !accessibleSpotsIds.contains(p.getActualStorageSpotId().toString())).toList();
        if (!notAccessibleProducts.isEmpty()){
            throw new SpaceNotAccessibleException("User with id " + command.userId() + " is not a participant of the space/s where the product/s with id " + String.join(", ", notAccessibleProducts.stream().map(p -> p.getId().toString()).toList()) + " are.");
        }

        productRepositoryPort.deleteAll(products, clock);
    }
}
