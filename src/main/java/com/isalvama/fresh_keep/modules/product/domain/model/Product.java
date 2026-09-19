package com.isalvama.fresh_keep.modules.product.domain.model;

import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidProductException;
import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidProductPriceUpdateException;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Product {
    private final ProductId id;
    private ProductName name;
    private LocalDate expirationDate;
    private final StorageSpotId suggestedStorageSpotId;
    private StorageSpotId actualStorageSpotId;
    private ProductType productType;
    private final ShoppingReceiptId shoppingReceiptId;
    private Money price;

    private Product(ProductId id, ProductName name, LocalDate expirationDate, StorageSpotId suggestedStorageSpotId, StorageSpotId actualStorageSpotId, ProductType productType, ShoppingReceiptId shoppingReceiptId, Money price) {
        this.id = validateNotNull(id, "id");
        this.name = validateNotNull(name, "name");
        this.expirationDate = validateNotNull(expirationDate, "expirationDate");
        this.suggestedStorageSpotId = validateNotNull(suggestedStorageSpotId, "suggestedStorageSpotId");
        this.actualStorageSpotId = validateNotNull(actualStorageSpotId, "actualStorageSpotId");
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
                suggestedStorageSpotId,
                productType,
                shoppingReceiptId,
                price);
    }

    public static Product reconstitute(ProductId id, ProductName name, LocalDate expirationDate, StorageSpotId suggestedStorageSpotId, StorageSpotId actualStorageSpotId, ProductType productType, ShoppingReceiptId shoppingReceiptId, Money price) {
        return new Product(
                id,
                name,
                expirationDate,
                suggestedStorageSpotId,
                actualStorageSpotId,
                productType,
                shoppingReceiptId,
                price);
    }

    public void updateStorageSpot(StorageSpotId newStorageSpotId, LocalDate newExpirationDate){
        this.actualStorageSpotId = validateNotNull(newStorageSpotId, "newStorageSpotId");
        this.expirationDate = validateNotNull(newExpirationDate, "newExpirationDate");
    }

    public void update(ProductName name, LocalDate expirationDate, ProductType productType,
                       Amount amount, Currency currency) {
        Money updatedPrice = updatedPrice(amount, currency);

        if (name != null) this.name = name;
        if (expirationDate != null) this.expirationDate = expirationDate;
        if (productType != null) this.productType = productType;
        if (updatedPrice != null) this.price = updatedPrice;
    }

    private Money updatedPrice(Amount amount, Currency currency) {
        if (amount == null && currency == null) return null;

        if (price == null && (amount == null || currency == null)) {
            throw new InvalidProductPriceUpdateException(
                    "amount and currency are both required when the product has no existing price.");
        }

        return new Money(
                amount != null ? amount : price.amount(),
                currency != null ? currency : price.currency());
    }

    private static <T> T validateNotNull(T fieldValue, String fieldName) {
        if (fieldValue == null)
            throw new InvalidProductException(fieldName + " cannot be null.");
        return fieldValue;
    }

}
