package com.isalvama.fresh_keep.modules.product.application.port.out.dto;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public record ProductMovedDto(
        String productName,
        String productType,
        LocalDate shoppingDate,
        List<ProductChangesDto> productChanges,
        Clock clock
) {
}
