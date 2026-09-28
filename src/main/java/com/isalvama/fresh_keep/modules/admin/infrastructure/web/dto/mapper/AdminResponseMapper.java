package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.admin.application.port.in.result.*;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.RegisteredUserDto;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminResponseMapper {

    public ProductMetricResponse toProductMetricResponse(UserRegistrationMetricResult metric) {
        return new ProductMetricResponse(metric.date(), metric.count());
    }

    public ProductMetricResponse toProductMetricResponse(ProductMetricResult metric) {
        return new ProductMetricResponse(metric.date(), metric.count());
    }

    public RegisteredUserResponse toRegisteredUserResponse(RegisteredUserDto user) {
        return new RegisteredUserResponse(user.id(), user.email(), user.username(),
                user.registeredAt(), user.lastLoggedAt());
    }

    public UserDetailsResponse toUserDetailsResponse(UserDetailsResult user) {
        List<UserDetailsResponse.SpaceSummary> spaces = user.spaces().stream()
                .map(space -> new UserDetailsResponse.SpaceSummary(space.id(), space.name()))
                .toList();
        List<UserDetailsResponse.ReceiptSummary> receipts = user.receipts().stream()
                .map(receipt -> new UserDetailsResponse.ReceiptSummary(
                        receipt.id(), receipt.createdAt(), receipt.purchaseDate(), receipt.storeName()))
                .toList();
        return new UserDetailsResponse(user.id(), user.email(), user.username(), user.registeredAt(),
                user.lastLoggedAt(), user.roles(), spaces, receipts);
    }

    public DailyReceiptSummaryResponse toDailyReceiptSummaryResponse(DailyReceiptSummaryResult summary) {
        return new DailyReceiptSummaryResponse(summary.date(), summary.totalReceipts());
    }

    public ShoppingReceiptSummaryResponse toShoppingReceiptSummaryResponse(ReceiptSummaryResult summary) {
        return new ShoppingReceiptSummaryResponse(summary.id(), summary.creatorId(), summary.spaceId(),
                summary.storeName(), summary.purchaseDate(), summary.createdAt());
    }

    public ShoppingReceiptDetailResponse toShoppingReceiptDetailResponse(ReceiptDetailResult detail) {
        List<ReceiptProductResponse> products = detail.products().stream()
                .map(this::toReceiptProductResponse)
                .toList();
        return new ShoppingReceiptDetailResponse(
                detail.id(), detail.creatorId(), detail.creatorUsername(), detail.creatorEmail(),
                detail.spaceId(), detail.spaceName(), detail.storeName(), detail.purchaseDate(), detail.createdAt(),
                detail.receiptImageId(), detail.receiptImageAssetId(), detail.receiptImageMimeType(), products);
    }

    public ReceiptProductResponse toReceiptProductResponse(ReceiptDetailResult.ProductResult product) {
        return new ReceiptProductResponse(product.id(), product.name(), product.expirationDate(),
                product.actualStorageSpotId(), product.productType(), product.price(), product.currency());
    }

    public ProductInfoResponse toProductInfoResponse(ProductResult product) {
        return new ProductInfoResponse(product.id(), product.name(), product.expirationDate(),
                product.actualStorageSpotId(), product.productType(), product.shoppingReceiptId(),
                product.price(), product.currency());
    }

    public ProductDetailResponse toProductDetailResponse(ProductDetailResult product) {
        return new ProductDetailResponse(
                product.id(), product.name(), product.productType(), product.expirationDate(),
                product.actualStorageSpotId(), product.storageSpotIdType(), product.creatorId(),
                product.creatorUsername(), product.creatorEmail(), product.spaceId(), product.spaceName(),
                product.storeName(), product.purchaseDate(), product.createdAt(), product.shoppingReceiptId(),
                product.price(), product.currency());
    }

    public ProductTypeCountResponse toProductTypeCountResponse(ProductTypeCountResult productType) {
        return new ProductTypeCountResponse(productType.productType(), productType.productCount());
    }

}
