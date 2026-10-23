package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shopping_receipt_query;

import com.isalvama.fresh_keep.modules.admin.application.port.out.ShoppingReceiptQueryPort;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ShoppingReceiptCreationDailyMetricDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptDetailDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptProductDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptsPageDto;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shopping_receipt_query.mapper.ShoppingReceiptQueryMapper;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.projection.MetricProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.query_builder.MetricsQueryBuilder;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shopping_receipt_query.query_builder.ShoppingReceiptQueryBuilder;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaShoppingReceiptEntity;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shopping_receipt_query.projection.ReceiptDetailProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shopping_receipt_query.projection.ReceiptSummaryProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.specification.SpecificationBuilder;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ShoppingReceiptQueryAdapter implements ShoppingReceiptQueryPort {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;
    private final ShoppingReceiptQueryBuilder receiptQueryBuilder;
    private final ShoppingReceiptQueryMapper mapper;
    private final MetricsQueryBuilder metricsQueryBuilder;


    @Override
    public List<ShoppingReceiptCreationDailyMetricDto> getShoppingReceiptMetrics (LocalDate from, LocalDate to, UUID spaceId, UUID creatorId) {
        PredicateSpecification<JpaShoppingReceiptEntity> filters =
                SpecificationBuilder.matchingFilters(from, to, spaceId, creatorId);

        JpaCriteriaQuery<MetricProjection> query = metricsQueryBuilder.build(JpaShoppingReceiptEntity.class, filters, "createdAt");
        List<MetricProjection> rows = entityManager.createQuery(query)
                .getResultList();

        if (rows.isEmpty()) return List.of();
        return mapper.toShoppingReceiptCreationDailyMetricDto(rows);
    }


    @Override
    public ReceiptsPageDto findReceipts(LocalDate from, LocalDate to, UUID spaceId, UUID creatorId,
                                        int offset, int limit) {
        PredicateSpecification<JpaShoppingReceiptEntity> filters =
                SpecificationBuilder.matchingFilters(from, to, spaceId, creatorId);

        JpaCriteriaQuery<ReceiptSummaryProjection> query = receiptQueryBuilder.buildReceiptSummaryProjectionQuery(filters);

        List<ReceiptSummaryProjection> rows = entityManager.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        if (rows.isEmpty()) return new ReceiptsPageDto(List.of(), 0);

        long totalElements = entityManager.createQuery(receiptQueryBuilder.countQuery(filters)).getSingleResult();
        return new ReceiptsPageDto(rows.stream().map(mapper::toSummaryDto).toList(), totalElements);
    }


    @Override
    public Optional<ReceiptDetailDto> findReceiptById(UUID receiptId) {
        JpaCriteriaQuery<ReceiptDetailProjection> query = receiptQueryBuilder.buildReceiptDetailProjectionQuery(receiptId);

        List<ReceiptDetailProjection> headers = entityManager.createQuery(query).getResultList();
        if (headers.isEmpty()) return Optional.empty();

        return Optional.of(mapper.toDetailDto(headers.getFirst(), findProductsByReceiptId(receiptId)));
    }

    // Products are a one-to-many list for a single receipt, so they can't be folded into the header's
    // constructor projection as a column value (a subquery there must return exactly one row/column,
    // like the COUNT above). They're also fetched via plain JDBC rather than Criteria on
    // JpaProductEntity, because that entity's @SQLRestriction("deleted_at IS NULL") would silently drop
    // deleted products from every Criteria/JPQL query against it - but this list must include them,
    // flagged via "deleted".
    private List<ReceiptProductDto> findProductsByReceiptId(UUID receiptId) {
        return jdbcTemplate.query("""
                SELECT p.id, p.name, p.expiration_date, p.actual_storage_spot_id,
                       p.product_type, p.price, p.currency, p.deleted_at IS NOT NULL AS deleted
                FROM products p
                WHERE p.shopping_receipt_id = :receiptId
                ORDER BY p.created_at ASC, p.id ASC
                """, Map.of("receiptId", receiptId), (rs, rowNum) -> new ReceiptProductDto(
                rs.getObject("id", UUID.class),
                rs.getString("name"),
                rs.getObject("expiration_date", LocalDate.class),
                rs.getObject("actual_storage_spot_id", UUID.class),
                rs.getString("product_type"),
                rs.getBigDecimal("price"),
                rs.getString("currency"),
                rs.getBoolean("deleted")));
    }
}
