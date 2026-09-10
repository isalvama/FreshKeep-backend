package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command;

import org.springframework.web.multipart.MultipartFile;

public record ProcessNewShoppingReceiptCommand(
        MultipartFile file,
        String creatorId,
        String spaceId,
        String language
) {
}
