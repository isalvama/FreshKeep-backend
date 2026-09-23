package com.isalvama.fresh_keep.modules.admin.domain.value_object;

import com.isalvama.fresh_keep.modules.admin.domain.exception.InvalidPaginationException;

public record Pagination(int offset, int limit) {

    private static final int MAX_LIMIT = 40;
    private static final int DEFAULT_LIMIT = 30;

    public Pagination {
        if (offset < 0) {
            throw new InvalidPaginationException("offset cannot be negative");
        }
        if (limit <= 0 || limit > MAX_LIMIT) {
            throw new InvalidPaginationException("The limit should be between 1 and " + MAX_LIMIT);
        }
    }

    public static Pagination fromPage(Integer page, Integer size) {
        int validPage = (page == null || page < 1) ? 1 : page;
        int validSize = (size == null || size <= 0) ? DEFAULT_LIMIT : Math.min(size, MAX_LIMIT);

        return new Pagination(
                calculateOffset(validPage, validSize),
                validSize
        );
    }

    private static int calculateOffset(int page, int size){
        return (page - 1) * size;
    }
}