package com.isalvama.fresh_keep.modules.product.infrastructure.product_categories_look_up;

import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Currency;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ProductCategoriesLookUpPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.CategoriesDto;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.isalvama.fresh_keep.modules.product.domain.model.ProductType.*;

@Component
public class ProductCategoriesLookUpAdapter implements ProductCategoriesLookUpPort {

    @Override
    public CategoriesDto getProductTypesAndMoneyCurrencyConstNames() {
        return new CategoriesDto(
                Arrays.stream(values()).map(Enum::name).toList(),
                Arrays.stream(Currency.values()).map(Enum::name).toList()
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
