package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jdbc;

import com.isalvama.fresh_keep.modules.admin.application.port.out.ShoppingReceiptQueryPort;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.DailyReceiptSummaryDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptDetailDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptProductDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptSummaryDto;
import lombok.RequiredArgsConstructor;
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
    public List<ReceiptSummaryDto> findReceipts(LocalDate from, LocalDate to, UUID spaceId, UUID creatorId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        StringBuilder query = new StringBuilder("""
                SELECT sr.id, sr.creator_id, sr.space_id, sr.store_name,
                       sr.purchase_date, sr.created_at
                FROM shopping_receipts sr
                WHERE 1 = 1
                """);
        appendFilters(query, parameters, from, to, spaceId, creatorId);
        query.append(" ORDER BY sr.created_at DESC, sr.id ASC");
        return jdbcTemplate.query(query.toString(), parameters, (rs, rowNum) ->
                new ReceiptSummaryDto(
                        rs.getObject("id", UUID.class),
                        rs.getObject("creator_id", UUID.class),
                        rs.getObject("space_id", UUID.class),
                        rs.getString("store_name"),
                        rs.getObject("purchase_date", Timestamp.class).toInstant().atZone(java.time.ZoneOffset.UTC).toLocalDate(),
                        rs.getObject("created_at", Timestamp.class).toInstant()));
    }

    @Override
    public Optional<ReceiptDetailDto> findReceiptById(UUID receiptId) {
        Map<String, UUID> parameters = Map.of("receiptId", receiptId);
        List<ReceiptDetailDto> receipts = jdbcTemplate.query("""
                SELECT sr.id, sr.creator_id, u.username AS creator_username, u.email AS creator_email,
                       sr.space_id, s.name AS space_name, sr.store_name, sr.purchase_date, sr.created_at,
                       sr.receipt_image_id, ri.asset_id AS receipt_image_asset_id, ri.mime_type AS receipt_image_mime_type
                FROM shopping_receipts sr
                LEFT JOIN users u ON u.id = sr.creator_id
                LEFT JOIN spaces s ON s.id = sr.space_id
                LEFT JOIN receipt_images ri ON ri.id = sr.receipt_image_id
                WHERE sr.id = :receiptId
                """, parameters, (rs, rowNum) -> new ReceiptDetailDto(
                rs.getObject("id", UUID.class),
                rs.getObject("creator_id", UUID.class),
                rs.getString("creator_username"),
                rs.getString("creator_email"),
                rs.getObject("space_id", UUID.class),
                rs.getString("space_name"),
                rs.getString("store_name"),
                rs.getObject("purchase_date", Timestamp.class).toInstant().atZone(java.time.ZoneOffset.UTC).toLocalDate(),
                rs.getObject("created_at", Timestamp.class).toInstant(),
                rs.getObject("receipt_image_id", UUID.class),
                rs.getString("receipt_image_asset_id"),
                rs.getString("receipt_image_mime_type"),
                List.of()));

        if (receipts.isEmpty()) return Optional.empty();
        ReceiptDetailDto receipt = receipts.getFirst();
        List<ReceiptProductDto> products = jdbcTemplate.query("""
                SELECT p.id, p.name, p.expiration_date, p.actual_storage_spot_id,
                       p.product_type, p.price, p.currency
                FROM products p
                WHERE p.shopping_receipt_id = :receiptId
                ORDER BY p.created_at ASC, p.id ASC
                """, parameters, (rs, rowNum) -> new ReceiptProductDto(
                rs.getObject("id", UUID.class),
                rs.getString("name"),
                rs.getObject("expiration_date", LocalDate.class),
                rs.getObject("actual_storage_spot_id", UUID.class),
                rs.getString("product_type"),
                rs.getBigDecimal("price"),
                rs.getString("currency")));

        return Optional.of(new ReceiptDetailDto(
                receipt.id(), receipt.creatorId(), receipt.creatorUsername(), receipt.creatorEmail(),
                receipt.spaceId(), receipt.spaceName(), receipt.storeName(), receipt.purchaseDate(),
                receipt.createdAt(), receipt.receiptImageId(), receipt.receiptImageAssetId(),
                receipt.receiptImageMimeType(), products));
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
