package com.isalvama.fresh_keep.modules.product.infrastructure.product_registration;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ProductRegistrationPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.RegisterProductDto;

import java.util.List;

public class ProductRegistrationAdapter implements ProductRegistrationPort {
    @Override
    public void registerProducts(List<RegisterProductDto> products) {

    }
}
