package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jdbc.dto;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public record QueryAppenderResult(
        StringBuilder query,
        MapSqlParameterSource parameters
) {
}
