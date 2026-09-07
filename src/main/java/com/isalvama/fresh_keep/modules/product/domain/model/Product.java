package com.isalvama.fresh_keep.modules.product.domain.model;

import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidProductException;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Money;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductName;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class Product {
    private final ProductId id;
    private final ProductName name;
    private LocalDate expirationDate;
    private StorageSpotId suggestedStorageSpotId;
    private StorageSpotId actualStorageSpotId;
    private ProductType productType;
    private final ShoppingReceiptId shoppingReceiptId;
    private final Money price;

    private Product(ProductId id, ProductName name, LocalDate expirationDate, StorageSpotId suggestedStorageSpotId, ProductType productType, ShoppingReceiptId shoppingReceiptId, Money price) {
        this.id = validateNotNull(id, "id");
        this.name = validateNotNull(name, "name");
        this.expirationDate = validateNotNull(expirationDate, "expirationDate");
        this.suggestedStorageSpotId = validateNotNull(suggestedStorageSpotId, "suggestedStorageSpotId");
        this.productType = validateNotNull(productType, "productType");
        this.shoppingReceiptId = validateNotNull(shoppingReceiptId, "shoppingReceiptId");
        this.price = price;
    }

    public static Product create(ProductName name, LocalDate expirationDate, StorageSpotId suggestedStorageSpotId, ProductType productType, ShoppingReceiptId shoppingReceiptId, Money price) {
        return new Product(
                ProductId.create(),
                name,
                expirationDate,
                suggestedStorageSpotId,
                productType,
                shoppingReceiptId,
                price);
    }

    private static <T> T validateNotNull(T fieldValue, String fieldName) {
        if (fieldValue == null)
            throw new InvalidProductException(fieldName + " cannot be null.");
        return fieldValue;
    }
}