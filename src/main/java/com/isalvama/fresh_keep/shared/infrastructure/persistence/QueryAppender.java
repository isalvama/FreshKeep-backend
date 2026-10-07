package com.isalvama.fresh_keep.shared.infrastructure.persistence;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.DailyMetricDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

@Component
public class QueryAppender {

    public QueryAppenderResult append(StringBuilder query, MapSqlParameterSource parameters,
                                      String table, Object field, String fieldName, String columnName) {

        query.append(String.format("\nAND %s.%s = :%s", table, columnName, fieldName));
        parameters.addValue(fieldName, field);

        return new QueryAppenderResult(query, parameters);
    }

    public String appendCommonFilters(String baseQuery, MapSqlParameterSource parameters, String dateAlias,
                                      String additionalFilter, boolean paginated) {
        StringBuilder query = new StringBuilder(baseQuery);
        if (parameters.hasValue("from")) {
            query.append(" AND ").append(dateAlias).append(".created_at >= :from");
        }
        if (parameters.hasValue("toExclusive")) {
            query.append(" AND ").append(dateAlias).append(".created_at < :toExclusive");
        }
        if (additionalFilter != null) {
            query.append(additionalFilter);
        }
        query.append(" GROUP BY metric_date ORDER BY metric_date ASC");
        if (paginated) {
            query.append(" LIMIT :limit OFFSET :offset");
        }
        return query.toString();
    }

    public MapSqlParameterSource parameters(LocalDate from, LocalDate to, int offset, int limit) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);
        if (from != null) {
            parameters.addValue("from", Timestamp.valueOf(from.atStartOfDay()));
        }
        if (to != null) {
            parameters.addValue("toExclusive", Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
        }
        return parameters;
    }

    public DailyMetricDto mapMetric(ResultSet resultSet, int rowNumber) throws SQLException {
        return new DailyMetricDto(
                resultSet.getObject("metric_date", LocalDate.class),
                resultSet.getLong("metric_count"));
    }


}
