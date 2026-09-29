package com.isalvama.fresh_keep.modules.product.domain.model;

import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidProductTypeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductTypeTest {
    @Test
    void getValueOf_returnsMatchingConstant() {
        assertEquals(ProductType.FRUITS, ProductType.getValueOf("FRUITS"));
    }

    @Test
    void getValueOf_throwsInvalidProductTypeExceptionWhenNameDoesNotMatchAnyConstant() {
        assertThrows(InvalidProductTypeException.class, () -> ProductType.getValueOf("NOT_A_PRODUCT_TYPE"));
    }

    @Test
    void getValueOf_throwsInvalidProductTypeExceptionWhenNameHasDifferentCase() {
        assertThrows(InvalidProductTypeException.class, () -> ProductType.getValueOf("fruits"));
    }

    @Test
    void listValuesNames_returnsAllConstantNames() {
        assertEquals(ProductType.values().length, ProductType.listValuesNames().size());
        assertTrue(ProductType.listValuesNames().contains("FRUITS"));
        assertTrue(ProductType.listValuesNames().contains("OTHER"));
    }
}
