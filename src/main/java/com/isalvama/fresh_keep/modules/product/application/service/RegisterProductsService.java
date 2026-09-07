package com.isalvama.fresh_keep.modules.product.application.service;

import com.isalvama.fresh_keep.modules.product.application.port.in.RegisterProductsUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.ProductsCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.RegisterProductsCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.result.RegisteredProductResult;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductRepositoryPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductStorageSpotHistoryRepositoryPort;
import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Money;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductName;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegisterProductsService implements RegisterProductsUseCase {
    private final ProductRepositoryPort productRepositoryPort;
    private final ProductStorageSpotHistoryRepositoryPort productStorageSpotHistoryRepositoryPort;

    @Override
    @Transactional
    public List<RegisteredProductResult> execute(RegisterProductsCommand command) {
        List<Product> products = command.products().stream().map(this::toProduct).toList();

        productRepositoryPort.saveAll(products);
        productStorageSpotHistoryRepositoryPort.saveAll(products, command.creatorId());

        return products.stream().map(this::toRegisteredProductResult).toList();
    }

    private Product toProduct(ProductsCommand p) {
        return Product.create(
                ProductName.from(p.productName()),
                p.expirationDate(),
                StorageSpotId.from(p.suggestedStorageSpotId()),
                ProductType.getValueOf(p.productType()),
                ShoppingReceiptId.from(p.shoppingReceiptId()),
                p.priceAmount() != null && p.currency() != null
                        ? Money.from(p.priceAmount(), p.currency())
                        : null
        );
    }

    private RegisteredProductResult toRegisteredProductResult(Product p) {
        return new RegisteredProductResult(
                p.getId().value(),
                p.getName().value(),
                p.getExpirationDate(),
                p.getSuggestedStorageSpotId().toString(),
                p.getProductType().name(),
                p.getPrice().amount().value(),
                p.getPrice().currency().name()
        );
    }
}
