package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.mapper;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.UserCreationDailyMetricDto;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.projection.MetricProjection;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductQueryMapper {

    public List<UserCreationDailyMetricDto> toUserCreationDailyMetricDto(List<MetricProjection> rows) {
        return rows.stream().map(row -> new UserCreationDailyMetricDto(row.date(), row.totalReceipts())).toList();
    }
}
