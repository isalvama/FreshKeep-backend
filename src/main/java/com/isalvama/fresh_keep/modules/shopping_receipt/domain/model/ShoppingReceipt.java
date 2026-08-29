package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.exception.InvalidShoppingReceiptException;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.Getter;
import lombok.Setter;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

@Getter
@Setter
public class ShoppingReceipt {
    private final ShoppingReceiptId id;
    private final UserId creatorId;
    private final SpaceId spaceId;
    private ReceiptImage receipt;
    private LocalDate purchaseDate;
    private String storeName;
    private Set<ProductId> productIds;

    private ShoppingReceipt(ShoppingReceiptId id, UserId creatorId, SpaceId spaceId, LocalDate purchaseDate, String storeName) {
        this.id = validateNotNull(id, "id");
        this.creatorId = validateNotNull(creatorId, "creatorId");
        this.spaceId = validateNotNull(spaceId, "spaceId");
        this.purchaseDate = validateNotNull(purchaseDate, "purchaseDate");
        this.storeName = storeName;
    }

    public static ShoppingReceipt create (
            UserId creatorId, SpaceId spaceId, LocalDate purchaseDate, String storeName, Clock clock
    ){
        if (purchaseDate.isAfter(LocalDate.now(clock))) {
            throw new InvalidShoppingReceiptException("The purchase date cannot be later than the current date");
        }
        return new ShoppingReceipt(
                ShoppingReceiptId.create(),
                creatorId,
                spaceId,
                purchaseDate,
                storeName
        );
    }
    public void addProductIds(Set<ProductId> productIds){
        this.productIds = validateNotNullAndNotEmpty(productIds, "productIds");
    }

    private static <T> T validateNotNull(T fieldValue, String fieldName) {
        if (fieldValue == null)
            throw new InvalidShoppingReceiptException(fieldName + " cannot be null.");
        return fieldValue;
    }

    private static <T extends Collection<?>> T validateNotNullAndNotEmpty(T fieldValue, String fieldName) {
        validateNotNull(fieldValue, fieldName);
        if (fieldValue.isEmpty()){
            throw new InvalidShoppingReceiptException(fieldName + " cannot be empty");
        }
        return fieldValue;
    }
}
