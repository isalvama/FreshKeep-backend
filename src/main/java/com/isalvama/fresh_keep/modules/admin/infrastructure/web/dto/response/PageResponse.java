package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PageResponse<T>(
        @Schema(description = "This page's rows") List<T> content,
        @Schema(description = "Current page number") int page,
        @Schema(description = "Page size") int size,
        @Schema(description = "Total matching rows across all pages") long totalElements,
        @Schema(description = "Total number of pages") int totalPages
) {
}
