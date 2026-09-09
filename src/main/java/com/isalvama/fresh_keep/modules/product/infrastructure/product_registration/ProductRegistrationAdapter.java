package com.isalvama.fresh_keep.modules.product.infrastructure.product_registration;

import com.isalvama.fresh_keep.modules.product.application.port.in.RegisterProductsUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.ProductsCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.RegisterProductsCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.result.RegisteredProductResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ProductRegistrationPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.RegisterProductDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.RegisteredProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductRegistrationAdapter implements ProductRegistrationPort {
    private final RegisterProductsUseCase registerProductsUseCase;

    @Override
    public List<RegisteredProductDto> registerProducts(List<RegisterProductDto> productsDtos) {
        RegisterProductsCommand command = new RegisterProductsCommand(
                productsDtos.getFirst().creatorId(),
                productsDtos.stream().map(this::toProductsCommand).toList()
        );

        List<RegisteredProductResult> registeredProducts = registerProductsUseCase.execute(command);

        return registeredProducts.stream().map(this::toRegisteredProductDto).toList();
    }

    private ProductsCommand toProductsCommand(RegisterProductDto p) {
        return new ProductsCommand(
                p.productName(),
                p.expirationDate(),
                p.suggestedStorageSpotId(),
                p.productType(),
                p.shoppingReceiptId(),
                p.priceAmount(),
                p.currency()
        );
    }

    private RegisteredProductDto toRegisteredProductDto(RegisteredProductResult r) {
        return new RegisteredProductDto(
                r.id(),
                r.productName(),
                r.expirationDate(),
                r.suggestedStorageSpotId(),
                r.productType(),
                r.priceAmount(),
                r.currency()
        );
    }
}
