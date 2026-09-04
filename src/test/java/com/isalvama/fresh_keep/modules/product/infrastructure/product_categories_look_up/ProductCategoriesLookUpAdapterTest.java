package com.isalvama.fresh_keep.modules.product.infrastructure.product_categories_look_up;

import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidProductTypeException;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Currency;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.CategoriesDto;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProductCategoriesLookUpAdapterTest {

    private final ProductCategoriesLookUpAdapter adapter = new ProductCategoriesLookUpAdapter();

    @Test
    void getProductTypesAndMoneyCurrencyConstNames_returnsAllProductTypeAndCurrencyConstNames() {
        CategoriesDto result = adapter.getProductTypesAndMoneyCurrencyConstNames();

        List<String> expectedProductTypes = Arrays.stream(ProductType.values()).map(Enum::name).toList();
        List<String> expectedCurrencies = Arrays.stream(Currency.values()).map(Enum::name).toList();

        assertEquals(expectedProductTypes, result.productTypes());
        assertEquals(expectedCurrencies, result.moneyCurrencies());
    }

    @Test
    void getPreferredStorageSpotTypeFor_returnsTheExpectedStorageSpotTypeForEveryProductType() {
        Map<ProductType, String> expected = new EnumMap<>(ProductType.class);
        expected.put(ProductType.DAIRY, "FRIDGE");
        expected.put(ProductType.MEAT, "FRIDGE");
        expected.put(ProductType.SEAFOOD, "FRIDGE");
        expected.put(ProductType.DELI, "FRIDGE");
        expected.put(ProductType.VEGETABLES, "FRIDGE");
        expected.put(ProductType.OTHER_FRESH_PRODUCTS, "FRIDGE");
        expected.put(ProductType.FROZEN_FOODS, "FREEZER");
        expected.put(ProductType.ICE_CREAM_AND_DESSERTS, "FREEZER");
        expected.put(ProductType.FRUITS, "FRUIT_BOWL");
        expected.put(ProductType.BAKERY, "COUNTERTOP");
        expected.put(ProductType.PANTRY, "PANTRY");
        expected.put(ProductType.SNACKS, "PANTRY");
        expected.put(ProductType.SWEETS, "PANTRY");
        expected.put(ProductType.BEVERAGES, "PANTRY");
        expected.put(ProductType.INTERNATIONAL, "PANTRY");
        expected.put(ProductType.SAUCES, "PANTRY");
        expected.put(ProductType.OTHER, "SHELF");

        assertEquals(ProductType.values().length, expected.size(), "every ProductType constant must be covered by this test");

        expected.forEach((productType, expectedStorageSpotType) ->
                assertEquals(expectedStorageSpotType, adapter.getPreferredStorageSpotTypeFor(productType.name()),
                        () -> "unexpected storage spot type for " + productType));
    }

    @Test
    void getPreferredStorageSpotTypeFor_throwsInvalidProductTypeExceptionWhenNameDoesNotMatchAnyProductType() {
        Exception exception = assertThrows(InvalidProductTypeException.class,
                () -> adapter.getPreferredStorageSpotTypeFor("NOT_A_PRODUCT_TYPE"));

        assertTrue(exception.getMessage().contains("does not match any ProductType constant name"));
    }
}
