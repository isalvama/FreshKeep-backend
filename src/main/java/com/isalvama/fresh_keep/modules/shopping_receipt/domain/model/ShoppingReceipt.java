package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.InvalidShoppingReceiptException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ReceiptImageId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.Getter;
import lombok.Setter;

import java.time.Clock;
import java.time.LocalDate;

@Getter
@Setter
public class ShoppingReceipt {
    private final ShoppingReceiptId id;
    private final UserId creatorId;
    private final SpaceId spaceId;
    private ReceiptImageId receiptImageId;
    private LocalDate purchaseDate;
    private String storeName;
    private ShoppingReceiptStatus status;

    private ShoppingReceipt(ShoppingReceiptId id, UserId creatorId, SpaceId spaceId, ReceiptImageId receiptImageId, LocalDate purchaseDate, String storeName, ShoppingReceiptStatus status) {
        this.id = validateNotNull(id, "id");
        this.creatorId = validateNotNull(creatorId, "creatorId");
        this.spaceId = validateNotNull(spaceId, "spaceId");
        this.receiptImageId = validateNotNull(receiptImageId, "receiptImageId");
        this.purchaseDate = validateNotNull(purchaseDate, "purchaseDate");
        this.storeName = storeName;
        this.status = status;
    }

    public static ShoppingReceipt createDraft(
            UserId creatorId, SpaceId spaceId, ReceiptImageId receiptImageId, LocalDate purchaseDate, String storeName, Clock clock
    ) {
        ShoppingReceipt shoppingReceipt = new ShoppingReceipt(
                ShoppingReceiptId.create(),
                creatorId,
                spaceId,
                receiptImageId,
                purchaseDate,
                storeName,
                ShoppingReceiptStatus.DRAFT
        );
        if (shoppingReceipt.purchaseDate.isAfter(LocalDate.now(clock))) {
            throw new InvalidShoppingReceiptException("The purchase date cannot be later than the current date");
        }
        return shoppingReceipt;
    }

    public static ShoppingReceipt reconstitute (
            ShoppingReceiptId id, UserId creatorId, SpaceId spaceId, ReceiptImageId receiptImageId, LocalDate purchaseDate, String storeName, ShoppingReceiptStatus status
    ) {
        return new ShoppingReceipt(
                id,
                creatorId,
                spaceId,
                receiptImageId,
                purchaseDate,
                storeName,
                status
        );
    }

    public void confirm(LocalDate purchaseDate, String storeName) {
        this.purchaseDate = validateNotNull(purchaseDate, "purchaseDate");
        this.storeName = storeName;
        this.status = ShoppingReceiptStatus.CONFIRMED;
    }

    private static <T> T validateNotNull(T fieldValue, String fieldName) {
        if (fieldValue == null)
            throw new InvalidShoppingReceiptException(fieldName + " cannot be null.");
        return fieldValue;
    }
}
