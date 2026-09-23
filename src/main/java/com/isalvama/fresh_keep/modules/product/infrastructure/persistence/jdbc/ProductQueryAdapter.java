package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jdbc;

import com.isalvama.fresh_keep.modules.admin.domain.criteria.ProductSortType;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductQueryPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductTypeCountDto;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductQueryAdapter implements ProductQueryPort {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ProductQueryResultSetExtractor productQueryResultSetExtractor;

    private static final String SPACE_PRODUCTS_QUERY = """
            SELECT p.id as id, p.name as name, p.expiration_date as expiration_date,
                   p.actual_storage_spot_id as actual_storage_spot_id, p.product_type as product_type,
                   p.shopping_receipt_id as shopping_receipt_id, p.price as price, p.currency as currency
            FROM products p
            JOIN storage_spots ss ON p.actual_storage_spot_id = ss.id
            WHERE ss.space_id = :spaceId
              AND p.deleted_at IS NULL
            ORDER BY p.expiration_date ASC
            """;

    private static final String ALL_PRODUCTS_QUERY = """
            SELECT p.id as id, p.name as name, p.expiration_date as expiration_date,
                   p.actual_storage_spot_id as actual_storage_spot_id, p.product_type as product_type,
                   p.shopping_receipt_id as shopping_receipt_id, p.price as price, p.currency as currency
            FROM products p
            WHERE p.deleted_at IS NULL
            """;

    private static final String PRODUCT_TYPES_BY_COUNT_QUERY = """
            SELECT p.product_type AS product_type, COUNT(*) AS product_count
            FROM products p
            WHERE p.deleted_at IS NULL
            GROUP BY p.product_type
            ORDER BY product_count DESC, product_type ASC
            """;

    @Override
    public List<ProductQueryDto> getSpaceProducts(UUID spaceId) {
        return jdbcTemplate.query(SPACE_PRODUCTS_QUERY, Map.of("spaceId", spaceId), productQueryResultSetExtractor);
    }

    @Override
    public List<ProductQueryDto> getAllProducts(ProductSortType sortType, Integer offset, Integer limit, ProductType productType) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);
        StringBuilder sql = new StringBuilder(ALL_PRODUCTS_QUERY);

        if (productType != null) {
            sql.append(" AND p.product_type = :productType");
            parameters.addValue("productType", productType.name());
        }

        sql.append(" ORDER BY ")
                .append(sortType.entityProperty())
                .append(" ")
                .append(sortType.orderType().name())
                .append(", p.id ASC LIMIT :limit OFFSET :offset");

        return jdbcTemplate.query(sql.toString(), parameters, productQueryResultSetExtractor);
    }

    @Override
    public List<ProductTypeCountDto> getProductTypesByCount() {
        return jdbcTemplate.query(PRODUCT_TYPES_BY_COUNT_QUERY, (rs, rowNum) ->
                new ProductTypeCountDto(
                        rs.getString("product_type"),
                        rs.getLong("product_count")));
    }
}
