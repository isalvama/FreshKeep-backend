package com.isalvama.fresh_keep.modules.product.application.port.out;

import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductMovedDto;

import java.time.LocalDate;

public interface ProductMovedExpirationDateCalculatorPort {
    LocalDate execute (ProductMovedDto dto);
}
