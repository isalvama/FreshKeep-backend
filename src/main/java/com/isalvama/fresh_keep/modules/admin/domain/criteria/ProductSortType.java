package com.isalvama.fresh_keep.modules.admin.domain.criteria;

public enum ProductSortType {
    NAME_ASC("name", OrderType.ASC),
    NAME_DESC("name", OrderType.DESC),
    EXPIRATION_DATE_ASC("expiration_date", OrderType.ASC),
    EXPIRATION_DATE_DESC("expiration_date", OrderType.DESC),
    PRICE_ASC("price", OrderType.ASC),
    PRICE_DESC("price", OrderType.DESC);

    private final String entityProperty;
    private final OrderType orderType;

    ProductSortType(String entityProperty, OrderType orderType){
        this.entityProperty = entityProperty;
        this.orderType = orderType;
    }

    public String entityProperty() {
        return entityProperty;
    }

    public OrderType orderType() {
        return orderType;
    }

    public static final String DEFAULT_SORT_VALUE = "NAME_ASC";
}
