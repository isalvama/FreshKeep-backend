package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query;

import com.isalvama.fresh_keep.modules.admin.application.port.out.UsersQueryPort;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.*;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.projection.MetricProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.query_builder.MetricsQueryBuilder;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.specification.SpecificationBuilder;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.mapper.UserQueryMapper;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.projection.UserDetailsProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.projection.UserReceiptProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.projection.UserSpaceProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.projection.UserSummaryProjection;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaShoppingReceiptEntity;
import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.entity.JpaUserEntity;
import com.isalvama.fresh_keep.shared.domain.Role;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class UserQueryAdapter implements UsersQueryPort {
    private final MetricsQueryBuilder metricsQueryBuilder;
    private final UserQueryBuilder userQueryBuilder;
    private final EntityManager entityManager;
    private final UserQueryMapper mapper;

    @Override
    public List<UserCreationDailyMetricDto> getUserRegistrations(LocalDate from, LocalDate to) {
        PredicateSpecification<JpaShoppingReceiptEntity> filters =
                SpecificationBuilder.matchingFromToFilters(from, to);

        JpaCriteriaQuery<MetricProjection> query = metricsQueryBuilder.build(filters, "createdAt");
        List<MetricProjection> rows = entityManager.createQuery(query)
                .getResultList();

        if (rows.isEmpty()) return List.of();
        return mapper.toUserCreationDailyMetricDto(rows);
    }

    @Override
    public RegisteredUsersPageDto findRegisteredUsers(LocalDate from, LocalDate to, int offset, int limit) {
        PredicateSpecification<JpaUserEntity> filters =
                SpecificationBuilder.matchingFromToFilters(from, to);

        JpaCriteriaQuery<UserSummaryProjection> query = userQueryBuilder.buildUserSummaryProjectionQuery(filters);

        List<UserSummaryProjection> rows = entityManager.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        if (rows.isEmpty()) return new RegisteredUsersPageDto(List.of(), 0);

        long totalElements = entityManager.createQuery(userQueryBuilder.countQuery(filters)).getSingleResult();
        return new RegisteredUsersPageDto(rows.stream().map(mapper::toSummaryDto).toList(), totalElements);
    }

    @Override
    public Optional<UserDetailsDto> findByUserId(UUID userId) {
        UserDetailsProjection user = entityManager.createQuery(userQueryBuilder.buildUserDetailProjectionQuery(userId)).getSingleResult();
        if (user == null) return Optional.empty();

        List<Role> roles = entityManager.createQuery(userQueryBuilder.buildUserRolesQuery(userId)).getResultList();

        List<UserReceiptProjection> receipts = entityManager.createQuery(userQueryBuilder.buildUserReceiptDetailProjectionQuery(userId)).getResultList();

        List<UserSpaceProjection> spaces = entityManager.createQuery(userQueryBuilder.buildUserSpaceDetailProjectionQuery(userId)).getResultList();

        return Optional.of(mapper.toDetailDto(user, roles, spaces, receipts));
    }
}
