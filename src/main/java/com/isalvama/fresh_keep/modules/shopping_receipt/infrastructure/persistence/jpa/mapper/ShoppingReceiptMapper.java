package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.mapper;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ReceiptImageId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaShoppingReceiptEntity;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class ShoppingReceiptMapper {
    private final Clock clock;

    public JpaShoppingReceiptEntity toEntity(ShoppingReceipt shoppingReceipt) {
        return JpaShoppingReceiptEntity.builder()
                .id(shoppingReceipt.getId().value())
                .creatorId(shoppingReceipt.getCreatorId().value())
                .spaceId(shoppingReceipt.getSpaceId().value())
                .receiptImageId(shoppingReceipt.getReceiptImageId().value())
                .purchaseDate(shoppingReceipt.getPurchaseDate().atStartOfDay(ZoneOffset.UTC).toInstant())
                .storeName(shoppingReceipt.getStoreName())
                .status(shoppingReceipt.getStatus())
                .build();
    }

    public ShoppingReceipt toDomain(JpaShoppingReceiptEntity entity) {
        return ShoppingReceipt.reconstitute(
                ShoppingReceiptId.of(entity.getId()),
                UserId.of(entity.getCreatorId()),
                SpaceId.of(entity.getSpaceId()),
                ReceiptImageId.of(entity.getReceiptImageId()),
                entity.getPurchaseDate().atZone(ZoneOffset.UTC).toLocalDate(),
                entity.getStoreName(),
                entity.getStatus()
        );
    }
}
