package com.isalvama.fresh_keep.modules.admin.application.port.in.result;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResult<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResult<>(content, page, size, totalElements, totalPages);
    }
}

