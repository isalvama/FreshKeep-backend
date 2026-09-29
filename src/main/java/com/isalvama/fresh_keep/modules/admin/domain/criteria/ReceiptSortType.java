package com.isalvama.fresh_keep.modules.admin.domain.criteria;

import com.isalvama.fresh_keep.modules.admin.domain.exception.InvalidReceiptSortException;

public enum ReceiptSortType {
    STORE_NAME_ASC("sr.store_name", OrderType.ASC),
    STORE_NAME_DESC("sr.store_name", OrderType.DESC),
    CREATED_AT_ASC("sr.created_at", OrderType.ASC),
    CREATED_AT_DESC("sr.created_at", OrderType.DESC);

    private final String column;
    private final OrderType orderType;

    ReceiptSortType(String column, OrderType orderType) {
        this.column = column;
        this.orderType = orderType;
    }

    public String column() {
        return column;
    }

    public OrderType orderType() {
        return orderType;
    }

    public static ReceiptSortType from(String value) {
        if (value == null || value.isBlank()) return CREATED_AT_DESC;

        String[] parts = value.split(",", -1);
        if (parts.length != 2) throw new InvalidReceiptSortException(value);

        try {
            return ReceiptSortType.valueOf(parts[0].trim().toUpperCase() + "_" + parts[1].trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidReceiptSortException(value);
        }
    }
}
