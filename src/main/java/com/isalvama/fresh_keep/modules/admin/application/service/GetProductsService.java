package com.isalvama.fresh_keep.modules.admin.application.service;

import com.isalvama.fresh_keep.modules.admin.application.command.GetProductMetricsCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetProductsCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.in.GetProductsUseCase;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ProductDetailResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ProductMetricResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ProductResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ProductTypeCountResult;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.GetAllProductsDto;
import com.isalvama.fresh_keep.modules.admin.domain.value_object.Pagination;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductQueryPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductDetailDto;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.exception.NonExistentProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetProductsService implements GetProductsUseCase {

    private final ProductQueryPort productQueryPort;

    @Override
    @Transactional(readOnly = true)
    public List<ProductMetricResult> getProductMetrics(GetProductMetricsCommand command) {
        return productQueryPort.getProducts(command.from(), command.to(), command.spaceId(), command.creatorId()).stream()
                .map(metric -> new ProductMetricResult(metric.date(), metric.count()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResult> getProducts(GetProductsCommand command) {

        Pagination pagination = Pagination.fromPage(command.page(), command.size());

        ProductType productType = command.productType() == null
                ? null
                : ProductType.getValueOf(command.productType());

        return productQueryPort.getAllProducts(new GetAllProductsDto(command.sort(), pagination.offset(), pagination.limit(), productType, toUuid(command.creatorId()), toUuid(command.shoppingReceiptId())))
                .stream()
                .map(product -> new ProductResult(
                        product.id(), product.name(), product.expirationDate(), product.actualStorageSpotId(),
                        product.productType(), product.shoppingReceiptId(), product.price(), product.currency()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResult getProduct(UUID productId) {
        ProductDetailDto product = productQueryPort.getProductById(productId)
                .orElseThrow(() -> new NonExistentProductException(
                        "Product with id " + productId + " does not exist."));
        return new ProductDetailResult(
                product.id(), product.name(), product.productType(), product.expirationDate(),
                product.actualStorageSpotId(), product.storageSpotIdType(), product.creatorId(),
                product.creatorUsername(), product.creatorEmail(), product.spaceId(), product.spaceName(),
                product.storeName(), product.purchaseDate(), product.createdAt(), product.shoppingReceiptId(),
                product.price(), product.currency());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductTypeCountResult> getProductTypes() {
        return productQueryPort.getProductTypesByCount().stream()
                .map(productType -> new ProductTypeCountResult(productType.productType(), productType.productCount()))
                .toList();
    }

    private static UUID toUuid(String id) {
        return id == null ? null : UUID.fromString(id);
    }
}
