package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.mapper;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ReceiptImage;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaReceiptImageEntity;
import org.springframework.stereotype.Component;

@Component
public class ReceiptImageMapper {
    public JpaReceiptImageEntity toEntity(ReceiptImage receiptImage){
        return JpaReceiptImageEntity.builder()
                .id(receiptImage.getId().value())
                .assetId(receiptImage.getAssetId().value())
                .build();
    }
}
