package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.CategoriesDto;

public interface ProductCategoriesLookUpPort {
    CategoriesDto getProductTypesAndMoneyCurrencyConstNames();
    String getPreferredStorageSpotTypeFor(String productTypeName);
}
