package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model;

import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
public class ShoppingReceipt {
    private final ShoppingReceiptId id;
    private final UserId userId;
    private final SpaceId spaceId;
    private final Receipt receipt;

    private Instant shoppingDate;
    private String purchasePlace;
    private Set<ProductId> productIds;

    public ShoppingReceipt(ShoppingReceiptId id, UserId userId, SpaceId locationId, Receipt receipt) {
        this.id = id;
        this.userId = userId;
        this.spaceId = locationId;
        this.receipt = receipt;
    }
}
