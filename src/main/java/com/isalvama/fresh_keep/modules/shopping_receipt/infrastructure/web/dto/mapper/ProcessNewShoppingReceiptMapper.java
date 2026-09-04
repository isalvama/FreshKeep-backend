package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProcessNewShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request.ProcessNewShoppingReceiptRequest;

public class ProcessNewShoppingReceiptMapper {
    public static ProcessNewShoppingReceiptCommand toCommand(String spaceId, ProcessNewShoppingReceiptRequest request, String userId){
        return new ProcessNewShoppingReceiptCommand(
                request.file(),
                userId,
                spaceId
        );
    }
}
