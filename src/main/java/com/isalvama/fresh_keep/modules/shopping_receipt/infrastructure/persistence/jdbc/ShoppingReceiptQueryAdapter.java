package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jdbc;

import com.isalvama.fresh_keep.modules.admin.application.port.out.ShoppingReceiptQueryPort;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.DailyReceiptSummaryDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptDetailDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptProductDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptsPageDto;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.ShoppingReceiptSummaryQueryBuilder;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.mapper.ShoppingReceiptQueryRepositoryMapper;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaShoppingReceiptEntity;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.projection.ReceiptDetailProjection;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.projection.ReceiptSummaryProjection;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.specification.ShoppingReceiptSpecifications;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ShoppingReceiptQueryAdapter implements ShoppingReceiptQueryPort {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;
    private final ShoppingReceiptSummaryQueryBuilder shoppingReceiptSummaryQueryBuilder;
    private final ShoppingReceiptQueryRepositoryMapper mapper;


    @Override
    public List<DailyReceiptSummaryDto> getShoppingReceiptMetrics (LocalDate from, LocalDate to, UUID spaceId, UUID creatorId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        StringBuilder query = new StringBuilder("""
                SELECT CAST(sr.purchase_date AS DATE) AS metric_date,
                       COUNT(*) AS total_receipts
                 FROM shopping_receipts sr
                 WHERE 1 = 1
                """);
        appendFilters(query, parameters, from, to, spaceId, creatorId);
        query.append(" GROUP BY metric_date ORDER BY metric_date ASC");

        return jdbcTemplate.query(query.toString(), parameters, (rs, rowNum) ->
                new DailyReceiptSummaryDto(
                        rs.getObject("metric_date", LocalDate.class),
                        rs.getLong("total_receipts")));
    }


    @Override
    public ReceiptsPageDto findReceipts(LocalDate from, LocalDate to, UUID spaceId, UUID creatorId,
                                        int offset, int limit) {

        PredicateSpecification<JpaShoppingReceiptEntity> filters =
                ShoppingReceiptSpecifications.matchingFilters(from, to, spaceId, creatorId);

        JpaCriteriaQuery<ReceiptSummaryProjection> query = shoppingReceiptSummaryQueryBuilder.buildReceiptSummaryProjectionQuery(filters);

        List<ReceiptSummaryProjection> rows = entityManager.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        if (rows.isEmpty()) return new ReceiptsPageDto(List.of(), 0);

        long totalElements = entityManager.createQuery(shoppingReceiptSummaryQueryBuilder.countQuery(filters)).getSingleResult();
        return new ReceiptsPageDto(rows.stream().map(mapper::toSummaryDto).toList(), totalElements);
    }


    @Override
    public Optional<ReceiptDetailDto> findReceiptById(UUID receiptId) {
        JpaCriteriaQuery<ReceiptDetailProjection> query = shoppingReceiptSummaryQueryBuilder.buildReceiptDetailProjectionQuery(receiptId);

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



    private void appendFilters(StringBuilder query, MapSqlParameterSource parameters,
                               LocalDate from, LocalDate to, UUID spaceId, UUID creatorId) {
        if (from != null) {
            query.append(" AND sr.purchase_date >= :from");
            parameters.addValue("from", Timestamp.valueOf(from.atStartOfDay()));
        }
        if (to != null) {
            query.append(" AND sr.purchase_date < :toExclusive");
            parameters.addValue("toExclusive", Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
        }
        if (spaceId != null) {
            query.append(" AND sr.space_id = :spaceId");
            parameters.addValue("spaceId", spaceId);
        }
        if (creatorId != null) {
            query.append(" AND sr.creator_id = :creatorId");
            parameters.addValue("creatorId", creatorId);
        }
    }
}
