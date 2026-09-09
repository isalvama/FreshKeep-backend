package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductCommand(
        LocalDate expirationDate,
        String productName,
        String suggestedStorageSpotId,
        String productType,
        BigDecimal priceAmount,
        String currency
) {

}
