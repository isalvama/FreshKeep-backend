package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.UserCreationDailyMetricDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.GetAllProductsDto;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.query_builder.ProductQueryBuilder;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.mapper.ProductQueryMapper;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.projection.ProductDetailsProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.projection.ProductSummaryProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.projection.ProductTypeCountProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.projection.MetricProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.query_builder.MetricsQueryBuilder;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.specification.SpecificationBuilder;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductEntity;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductQueryPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductDetailDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductTypeCountDto;
import com.isalvama.fresh_keep.modules.product.infrastructure.exception.ProductPersistenceException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductQueryAdapter implements ProductQueryPort {
    private final MetricsQueryBuilder metricsQueryBuilder;
    private final ProductQueryBuilder productQueryBuilder;
    private final EntityManager entityManager;
    private final ProductQueryMapper mapper;

    @Override
    public List<ProductQueryDto> getSpaceProducts(UUID spaceId) {
        JpaCriteriaQuery<ProductSummaryProjection> query = productQueryBuilder.buildProductsBySpaceQuery(spaceId);
        return mapper.toProductQueryDto(entityManager.createQuery(query).getResultList());
    }

    @Override
    public List<ProductQueryDto> getAllProducts(GetAllProductsDto dto) {
        PredicateSpecification<JpaProductEntity> filters = PredicateSpecification.allOf(
                SpecificationBuilder.withProductType(dto.productType()),
                SpecificationBuilder.withShoppingReceipt(dto.shoppingReceiptId()));

        JpaCriteriaQuery<ProductSummaryProjection> query =
                productQueryBuilder.buildProductSummaryProjectionQuery(filters, dto.creatorId(), dto.sortType());

        try {
        List<ProductSummaryProjection> rows = entityManager.createQuery(query)
                .setFirstResult(dto.offset())
                .setMaxResults(dto.limit())
                .getResultList();

        if (rows.isEmpty()) return List.of();

        return mapper.toProductQueryDto(rows);

        } catch (DataAccessException e) {
            throw new ProductPersistenceException("Failed to retrieve products for the given filters: " + e.getMessage());
        }
    }

    @Override
    public Optional<ProductDetailDto> getProductById(UUID productId) {
        JpaCriteriaQuery<ProductDetailsProjection> query = productQueryBuilder.buildProductDetailProjectionQuery(productId);
        return entityManager.createQuery(query).getResultList().stream()
                .findFirst()
                .map(mapper::toProductDetailDto);
    }

    @Override
    public List<ProductTypeCountDto> getProductTypesByCount() {
        JpaCriteriaQuery<ProductTypeCountProjection> query = productQueryBuilder.buildProductTypeCountQuery();
        return mapper.toProductTypeCountDto(entityManager.createQuery(query).getResultList());
    }

    @Override
    public List<UserCreationDailyMetricDto> getProductsMetrics(LocalDate from, LocalDate to, UUID spaceId, UUID creatorId) {
        PredicateSpecification<JpaProductEntity> filters =
                productQueryBuilder.matchingReceiptFilters(from, to, spaceId, creatorId);

        JpaCriteriaQuery<MetricProjection> query = metricsQueryBuilder.build(JpaProductEntity.class, filters, "createdAt");
        List<MetricProjection> rows = entityManager.createQuery(query)
                .getResultList();

        if (rows.isEmpty()) return List.of();
        return mapper.toUserCreationDailyMetricDto(rows);
    }

}
