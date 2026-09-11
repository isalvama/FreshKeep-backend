package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jdbc;

import com.isalvama.fresh_keep.modules.product.application.port.out.ProductQueryPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductQueryAdapter implements ProductQueryPort {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ProductQueryResultSetExtractor spaceQueryResultSetExtractor;

    String sqlQuery = """
    SELECT
    p.id as id,
    p.name as name,
    p.expiration_date as expiration_date,
    p.actual_storage_spot_id as actual_storage_spot_id,
    p.product_type as product_type,
    p.shopping_receipt_id as shopping_receipt_id,
    p.price as price,
    p.currency as currency
        FROM products p
            JOIN storage_spots ss
            ON p.actual_storage_spot_id = ss.id
               WHERE ss.space_id = :spaceId
                    ORDER BY p.expiration_date ASC
    """;

    @Override
    public List<ProductQueryDto> getSpaceProducts(UUID spaceId) {
        return jdbcTemplate.query(sqlQuery, Map.of("spaceId", spaceId), spaceQueryResultSetExtractor);
    }
}
