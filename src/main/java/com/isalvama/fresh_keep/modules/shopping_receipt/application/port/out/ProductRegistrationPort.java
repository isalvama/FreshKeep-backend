package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.RegisterProductDto;

import java.util.List;

public interface ProductRegistrationPort {
    void registerProducts(List<RegisterProductDto> products);
}
