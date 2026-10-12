package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.mapper;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.UserDetailsDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.UserSummaryDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.UserCreationDailyMetricDto;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.projection.MetricProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.projection.UserDetailsProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.projection.UserReceiptProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.projection.UserSpaceProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.projection.UserSummaryProjection;
import com.isalvama.fresh_keep.shared.domain.Role;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

@Component
public class UserQueryMapper {

    public List<UserCreationDailyMetricDto> toUserCreationDailyMetricDto(List<MetricProjection> rows) {
        return rows.stream().map(row -> new UserCreationDailyMetricDto(row.date(), row.totalReceipts())).toList();
    }

    public UserSummaryDto toSummaryDto(UserSummaryProjection row) {
        return new UserSummaryDto(
                row.id(),
                row.email(),
                row.username(),
                row.registeredAt(),
                row.lastLoggedAt()
        );
    }

    public UserDetailsDto toDetailDto(UserDetailsProjection header, List<Role> roles,
                                       List<UserSpaceProjection> spaces, List<UserReceiptProjection> receipts) {
        return new UserDetailsDto(
                header.id(), header.email(), header.username(), header.registeredAt(), header.lastLoggedAt(),
                roles.stream().map(Role::name).toList(),
                spaces.stream().map(s -> new UserDetailsDto.UserSpaceDto(s.id(), s.name())).toList(),
                receipts.stream().map(r -> new UserDetailsDto.UserReceiptDto(
                        r.id(), r.createdAt(), r.purchaseDate().atZone(ZoneOffset.UTC).toLocalDate(), r.storeName())).toList());
    }
}
