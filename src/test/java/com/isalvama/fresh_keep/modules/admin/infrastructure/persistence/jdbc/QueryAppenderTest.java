package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jdbc;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class QueryAppenderTest {
    private final QueryAppender appender = new QueryAppender();

    @Test
    void append_addsFilterAndParameter() {
        StringBuilder query = new StringBuilder("SELECT * FROM products p WHERE 1 = 1");
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        appender.append(query, parameters, "p", "DAIRY", "productType", "product_type");

        assertTrue(query.toString().contains("AND p.product_type = :productType"));
        assertEquals("DAIRY", parameters.getValue("productType"));
    }

    @Test
    void parameters_addsDateBoundsAndPagination() {
        MapSqlParameterSource parameters = appender.parameters(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10), 20, 10);

        assertNotNull(parameters.getValue("from"));
        assertNotNull(parameters.getValue("toExclusive"));
        assertEquals(20, parameters.getValue("offset"));
        assertEquals(10, parameters.getValue("limit"));
    }

    @Test
    void appendCommonFilters_addsDatesAdditionalFilterGroupingAndPagination() {
        MapSqlParameterSource parameters = appender.parameters(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10), 0, 10);

        String query = appender.appendCommonFilters(
                "SELECT metric_date, metric_count FROM metrics WHERE 1 = 1",
                parameters, "m", " AND m.creator_id = :creatorId", true);

        assertTrue(query.contains("m.created_at >= :from"));
        assertTrue(query.contains("m.created_at < :toExclusive"));
        assertTrue(query.contains("m.creator_id = :creatorId"));
        assertTrue(query.contains("GROUP BY metric_date ORDER BY metric_date ASC"));
        assertTrue(query.contains("LIMIT :limit OFFSET :offset"));
    }
}
