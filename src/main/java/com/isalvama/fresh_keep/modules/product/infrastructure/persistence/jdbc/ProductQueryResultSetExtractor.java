package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jdbc;

import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ProductQueryResultSetExtractor implements ResultSetExtractor<List<ProductQueryDto>> {

    @Override
    public List<ProductQueryDto> extractData(ResultSet rs) throws SQLException, DataAccessException {

        List<ProductQueryDto> products = new ArrayList<>();

        while (rs.next()) {
            ProductQueryDto product = new ProductQueryDto(
                    rs.getObject("id", UUID.class),
                    rs.getObject("name", String.class),
                    rs.getObject("expiration_date", LocalDate.class),
                    rs.getObject("actual_storage_spot_id", UUID.class),
                    rs.getObject("product_type", String.class),
                    rs.getObject("shopping_receipt_id", UUID.class),
                    rs.getObject("price", BigDecimal.class),
                    rs.getObject("currency", String.class)
            );
            products.add(product);
        }
        return products;
    }
}
