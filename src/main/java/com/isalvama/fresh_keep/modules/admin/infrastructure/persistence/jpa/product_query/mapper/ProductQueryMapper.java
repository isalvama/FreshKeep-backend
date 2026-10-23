package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.mapper;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.UserCreationDailyMetricDto;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.projection.ProductDetailsProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.projection.ProductSummaryProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.projection.ProductTypeCountProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.projection.MetricProjection;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductDetailDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductTypeCountDto;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

@Component
public class ProductQueryMapper {

    public List<UserCreationDailyMetricDto> toUserCreationDailyMetricDto(List<MetricProjection> rows) {
        return rows.stream().map(row -> new UserCreationDailyMetricDto(row.date(), row.totalReceipts())).toList();
    }

    public List<ProductQueryDto> toProductQueryDto (List<ProductSummaryProjection> rows){
        return rows.stream().map(row -> new ProductQueryDto(
                row.id(),
                row.name(),
                row.expirationDate(),
                row.actualStorageSpotId(),
                row.productType().name(),
                row.shoppingReceiptId(),
                row.price(),
                row.currency() == null ? null : row.currency().name()
                )).toList();
    }

    public ProductDetailDto toProductDetailDto(ProductDetailsProjection row) {
        return new ProductDetailDto(
                row.id(),
                row.name(),
                row.productType().name(),
                row.expirationDate(),
                row.actualStorageSpotId(),
                row.storageSpotIdType() == null ? null : row.storageSpotIdType().name(),
                row.creatorId(),
                row.creatorUsername(),
                row.creatorEmail(),
                row.spaceId(),
                row.spaceName(),
                row.storeName(),
                row.purchaseDate() == null ? null : row.purchaseDate().atZone(ZoneOffset.UTC).toLocalDate(),
                row.createdAt(),
                row.shoppingReceiptId(),
                row.price(),
                row.currency() == null ? null : row.currency().name());
    }

    public List<ProductTypeCountDto> toProductTypeCountDto(List<ProductTypeCountProjection> rows) {
        return rows.stream()
                .map(row -> new ProductTypeCountDto(row.productType().name(), row.productCount()))
                .toList();
    }
}
