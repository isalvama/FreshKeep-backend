package com.isalvama.fresh_keep.shared.infrastructure.persistence;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public record QueryAppenderResult(
        StringBuilder query,
        MapSqlParameterSource parameters
) {
}
