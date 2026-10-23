package com.isalvama.fresh_keep.modules.product.infrastructure.product_categories_look_up;

import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Currency;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ProductCategoriesLookUpPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.CategoriesDto;
import org.springframework.stereotype.Component;

@Component
public class ProductCategoriesLookUpAdapter implements ProductCategoriesLookUpPort {

    @Override
    public CategoriesDto getProductTypesAndMoneyCurrencyConstNames() {
        return new CategoriesDto(
               ProductType.listValuesNames(),
               Currency.listValuesNames()
        );
    }

    @Override
    public String getPreferredStorageSpotTypeFor(String productTypeName) {
        ProductType productType = ProductType.getValueOf(productTypeName);
        return switch (productType) {
            case DAIRY, MEAT, SEAFOOD, DELI, VEGETABLES, OTHER_FRESH_PRODUCTS -> "FRIDGE";
            case FROZEN_FOODS, ICE_CREAM_AND_DESSERTS -> "FREEZER";
            case FRUITS -> "FRUIT_BOWL";
            case BAKERY -> "COUNTERTOP";
            case PANTRY, SNACKS, SWEETS, BEVERAGES, INTERNATIONAL, SAUCES -> "PANTRY";
            case OTHER -> "SHELF";
        };
    }
}
