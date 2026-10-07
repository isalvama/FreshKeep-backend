package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jdbc;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.DailyMetricDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.GetAllProductsDto;
import com.isalvama.fresh_keep.shared.infrastructure.persistence.QueryAppender;
import com.isalvama.fresh_keep.shared.infrastructure.persistence.QueryAppenderResult;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductQueryPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductDetailDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductTypeCountDto;
import com.isalvama.fresh_keep.modules.product.infrastructure.exception.ProductPersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductQueryAdapter implements ProductQueryPort {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ProductQueryResultSetExtractor productQueryResultSetExtractor;
    private final QueryAppender appender;

    private static final String PRODUCTS_QUERY = """
            SELECT CAST(p.created_at AS DATE) AS metric_date, COUNT(*) AS metric_count
            FROM products p
            LEFT JOIN shopping_receipts sr ON sr.id = p.shopping_receipt_id
            WHERE 1 = 1
            """;

    private static final String SPACE_PRODUCTS_QUERY = """
            SELECT p.id as id, p.name as name, p.expiration_date as expiration_date,
                   p.actual_storage_spot_id as actual_storage_spot_id, p.product_type as product_type,
                   p.shopping_receipt_id as shopping_receipt_id, p.price as price, p.currency as currency
            FROM products p
            JOIN storage_spots ss ON p.actual_storage_spot_id = ss.id
            WHERE ss.space_id = :spaceId
              AND p.deleted_at IS NULL
            ORDER BY p.expiration_date ASC, p.name ASC
            """;

    private static final String ALL_PRODUCTS_QUERY = """
            SELECT p.id as id, p.name as name, p.expiration_date as expiration_date,
                   p.actual_storage_spot_id as actual_storage_spot_id, p.product_type as product_type,
                   p.shopping_receipt_id as shopping_receipt_id, p.price as price, p.currency as currency
            FROM products p
            """;

    private static final String CREATOR_RECEIPTS_JOIN = "JOIN shopping_receipts sr ON sr.id = p.shopping_receipt_id\n";

    private static final String NOT_DELETED_CONDITION = "WHERE p.deleted_at IS NULL";

    private static final String PRODUCT_TYPES_BY_COUNT_QUERY = """
            SELECT p.product_type AS product_type, COUNT(*) AS product_count
            FROM products p
            WHERE p.deleted_at IS NULL
            GROUP BY p.product_type
            ORDER BY product_count DESC, product_type ASC
            """;

    private static final String GET_PRODUCT_BY_ID_QUERY = """
                SELECT p.id, p.name, p.product_type, p.expiration_date,
                       p.actual_storage_spot_id, ss.storage_spot_type,
                       sr.creator_id, u.username AS creator_username, u.email AS creator_email,
                       sr.space_id, s.name AS space_name, sr.store_name, sr.purchase_date,
                       p.created_at, p.shopping_receipt_id, p.price, p.currency
                FROM products p
                JOIN storage_spots ss ON ss.id = p.actual_storage_spot_id
                LEFT JOIN shopping_receipts sr ON sr.id = p.shopping_receipt_id
                LEFT JOIN users u ON u.id = sr.creator_id
                LEFT JOIN spaces s ON s.id = sr.space_id
                WHERE p.id = :productId
                  AND p.deleted_at IS NULL
                """;

    @Override
    public List<ProductQueryDto> getSpaceProducts(UUID spaceId) {
        try {
            return jdbcTemplate.query(SPACE_PRODUCTS_QUERY, Map.of("spaceId", spaceId), productQueryResultSetExtractor);
        } catch (DataAccessException e) {
            throw new ProductPersistenceException(
                    "Failed to retrieve products for space with id " + spaceId + ": " + e.getMessage());
        }
    }

    @Override
    public List<ProductQueryDto> getAllProducts(GetAllProductsDto dto) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("limit", dto.limit())
                .addValue("offset", dto.offset());
        StringBuilder sql = new StringBuilder(ALL_PRODUCTS_QUERY);

        // Joins must come before the WHERE; every filter is then an AND condition.
        if (dto.creatorId() != null) {
            sql.append(CREATOR_RECEIPTS_JOIN);
        }
        sql.append(NOT_DELETED_CONDITION);

        if (dto.creatorId() != null) {
            sql.append("\nAND sr.creator_id = :creatorId");
            parameters.addValue("creatorId", dto.creatorId());
        }

        if (dto.productType() != null) {
            QueryAppenderResult result = appender.append(sql, parameters, "p", dto.productType().name(), "productType", "product_type");
            sql = result.query();
            parameters = result.parameters();
        }

        if (dto.shoppingReceiptId() != null) {
            QueryAppenderResult result = appender.append(sql, parameters, "p", dto.shoppingReceiptId(), "shoppingReceiptId", "shopping_receipt_id");
            sql = result.query();
            parameters = result.parameters();
        }

        sql.append(" ORDER BY ")
                .append(dto.sortType().entityProperty())
                .append(" ")
                .append(dto.sortType().orderType().name())
                .append(", p.id ASC LIMIT :limit OFFSET :offset");

        try {
            return jdbcTemplate.query(sql.toString(), parameters, productQueryResultSetExtractor);
        } catch (DataAccessException e) {
            throw new ProductPersistenceException("Failed to retrieve products for the given filters: " + e.getMessage());
        }
    }

    @Override
    public Optional<ProductDetailDto> getProductById(UUID productId) {
        try {
            List<ProductDetailDto> products = jdbcTemplate.query(
                    GET_PRODUCT_BY_ID_QUERY, Map.of("productId", productId), (rs, rowNum) ->
                            new ProductDetailDto(
                    rs.getObject("id", UUID.class),
                    rs.getString("name"),
                    rs.getString("product_type"),
                    rs.getObject("expiration_date", LocalDate.class),
                    rs.getObject("actual_storage_spot_id", UUID.class),
                    rs.getString("storage_spot_type"),
                    rs.getObject("creator_id", UUID.class),
                    rs.getString("creator_username"),
                    rs.getString("creator_email"),
                    rs.getObject("space_id", UUID.class),
                    rs.getString("space_name"),
                    rs.getString("store_name"),
                    toLocalDate(rs.getObject("purchase_date", Timestamp.class)),
                    rs.getObject("created_at", Timestamp.class).toInstant(),
                    rs.getObject("shopping_receipt_id", UUID.class),
                    rs.getBigDecimal("price"),
                    rs.getString("currency")));
            return products.stream().findFirst();
        } catch (DataAccessException e) {
            throw new ProductPersistenceException("Failed to retrieve product with id " + productId + ": " + e.getMessage());
        }
    }

    private static LocalDate toLocalDate(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
    }

    @Override
    public List<ProductTypeCountDto> getProductTypesByCount() {
        try {
            return jdbcTemplate.query(PRODUCT_TYPES_BY_COUNT_QUERY, (rs, rowNum) ->
                    new ProductTypeCountDto(
                            rs.getString("product_type"),
                            rs.getLong("product_count")));
        } catch (DataAccessException e) {
            throw new ProductPersistenceException("Failed to retrieve product type counts: " + e.getMessage());
        }
    }

    @Override
    public List<DailyMetricDto> getProducts(LocalDate from, LocalDate to, UUID spaceId, UUID creatorId) {
        MapSqlParameterSource parameters = appender.parameters(from, to, 0, 0);
        StringBuilder filters = new StringBuilder();
        if (spaceId != null) {
            filters.append(" AND sr.space_id = :spaceId");
            parameters.addValue("spaceId", spaceId);
        }
        if (creatorId != null) {
            filters.append(" AND sr.creator_id = :creatorId");
            parameters.addValue("creatorId", creatorId);
        }
        String query = appender.appendCommonFilters(PRODUCTS_QUERY, parameters, "p", filters.toString(), false);
        try {
            return jdbcTemplate.query(query, parameters, appender::mapMetric);
        } catch (DataAccessException e) {
            throw new ProductPersistenceException("Failed to retrieve daily product metrics: " + e.getMessage());
        }
    }

}
