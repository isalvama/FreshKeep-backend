package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.RegisterProductDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.RegisteredProductDto;

import java.util.List;

public interface ProductRegistrationPort {
    List<RegisteredProductDto> registerProducts(List<RegisterProductDto> productsDtos);
}
