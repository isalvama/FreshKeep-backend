package com.isalvama.fresh_keep.modules.space.application.port.in.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpaceProductResult (
        String id,
        String productName,
        LocalDate expirationDate,
        String storageSpotId,
        String productType,
        BigDecimal priceAmount,
        String currency
){
}
