package com.isalvama.fresh_keep.modules.admin.application.port.out.dto;

import java.util.List;

public record ReceiptsPageDto(
        List<ReceiptSummaryDto> content,
        long totalElements
) {
}
