package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto;

import java.util.List;

public record CategoriesDto(
        List<String> productTypes,
        List<String> moneyCurrencies
) {
}
