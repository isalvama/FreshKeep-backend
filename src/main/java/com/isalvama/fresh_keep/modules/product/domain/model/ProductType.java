package com.isalvama.fresh_keep.modules.product.domain.model;

import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidProductTypeException;

public enum ProductType {
    FRUITS,
    VEGETABLES,
    OTHER_FRESH_PRODUCTS,
    MEAT,
    SEAFOOD,
    DAIRY,
    DELI,
    BAKERY,
    PANTRY,
    SNACKS,
    SWEETS,
    FROZEN_FOODS,
    ICE_CREAM_AND_DESSERTS,
    BEVERAGES,
    INTERNATIONAL,
    SAUCES,
    OTHER;

    public static ProductType getValueOf(String productTypeName) {
        try {
            return ProductType.valueOf(productTypeName);
        } catch (IllegalArgumentException e){
            throw new InvalidProductTypeException(productTypeName + "does not match any ProductType constant name.");
        }
    }
}


