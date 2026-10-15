package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.query_builder;

import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.entity.JpaAccountEntity;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.OrderTypeCriteria;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.projection.UserDetailsProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.projection.UserReceiptProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.projection.UserSpaceProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.projection.UserSummaryProjection;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaShoppingReceiptEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceParticipantEntity;
import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.entity.JpaUserEntity;
import com.isalvama.fresh_keep.shared.domain.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserQueryBuilder {
    private final EntityManager entityManager;

    public JpaCriteriaQuery<UserSummaryProjection> buildUserSummaryProjectionQuery (PredicateSpecification<JpaUserEntity> filters){
        BaseQuery<UserSummaryProjection> base = base(UserSummaryProjection.class);

        List<Selection<?>> baseSelection = new ArrayList<>(buildBaseSelection(base.cb(), base.user()));

        JpaCriteriaQuery<UserSummaryProjection> query = base.query().select(base.cb().construct(UserSummaryProjection.class, baseSelection.toArray(new Selection[0])));

        query.where(restriction(filters, base.user(), base.cb()));

        LinkedHashMap<String, OrderTypeCriteria> order = new LinkedHashMap<>();
        order.put("createdAt", OrderTypeCriteria.DESC);
        order.put("id", OrderTypeCriteria.ASC);

        return setOrder(query, base.cb(), base.user(), order);
    }

    public JpaCriteriaQuery<UserDetailsProjection> buildUserDetailProjectionQuery (UUID userId){
        BaseQuery<UserDetailsProjection> base = base(UserDetailsProjection.class);

        List<Selection<?>> selection = new ArrayList<>(buildBaseSelection(base.cb(), base.user()));

        JpaCriteriaQuery<UserDetailsProjection> query = base.query().select(base.cb().construct(UserDetailsProjection.class, selection.toArray(new Selection[0])));
        return query.where((base.cb().equal(base.user().get("id"), userId)));
    }

    public JpaCriteriaQuery<UserReceiptProjection> buildUserReceiptDetailProjectionQuery (UUID userId){
        HibernateCriteriaBuilder cb = criteriaBuilder();
        JpaCriteriaQuery<UserReceiptProjection> query = cb.createQuery(UserReceiptProjection.class);
        JpaRoot<JpaShoppingReceiptEntity> receipt = query.from(JpaShoppingReceiptEntity.class);

        return query.select(cb.construct(UserReceiptProjection.class,
                        receipt.get("id"), receipt.get("createdAt"), receipt.get("purchaseDate"), receipt.get("storeName")))
                .where(cb.equal(receipt.get("creatorId"), userId));
    }

    public JpaCriteriaQuery<UserSpaceProjection> buildUserSpaceDetailProjectionQuery (UUID userId){
        HibernateCriteriaBuilder cb = criteriaBuilder();
        JpaCriteriaQuery<UserSpaceProjection> query = cb.createQuery(UserSpaceProjection.class);
        JpaRoot<JpaSpaceParticipantEntity> participant = query.from(JpaSpaceParticipantEntity.class);

        JpaJoin<JpaSpaceParticipantEntity, JpaSpaceEntity> space = participant.join("space", JoinType.LEFT);

        return query.select(cb.construct(UserSpaceProjection.class, space.get("id"), space.get("name")))
                .where(cb.equal(participant.get("id").get("participantId"), userId));
    }

    public JpaCriteriaQuery<Role> buildUserRolesQuery(UUID userId) {
        BaseQuery<Role> base = base(Role.class);
        JpaJoin<JpaUserEntity, JpaAccountEntity> account = jpaEntityLeftJoin(JpaAccountEntity.class, "accountId", base.user(), base.cb());
        JpaJoin<JpaAccountEntity, Role> role = account.join("roles", JoinType.LEFT);

        return base.query().select(role).where(base.cb().equal(base.user().get("id"), userId));
    }

    public JpaCriteriaQuery<Long> countQuery(PredicateSpecification<JpaUserEntity> filters) {
        BaseQuery<Long> base = base(Long.class);
        return base.query().select(base.cb().count(base.user())).where(restriction(filters, base.user(), base.cb()));
    }

    public List<Selection<?>> buildBaseSelection(HibernateCriteriaBuilder cb, JpaRoot<JpaUserEntity> user) {
        JpaJoin<JpaUserEntity, JpaAccountEntity> account = jpaEntityLeftJoin(JpaAccountEntity.class, "accountId", user, cb);
        return List.of(
                user.get("id"),
                user.get("email"),
                user.get("userName"),
                user.get("createdAt"),
                account.get("lastLogIn")
        );
    }

    public Predicate restriction(PredicateSpecification<JpaUserEntity> filters,
                                 JpaRoot<JpaUserEntity> user, HibernateCriteriaBuilder cb) {
        Predicate predicate = filters.toPredicate(user, cb);
        return predicate != null ? predicate : cb.conjunction();
    }

    public <T> JpaCriteriaQuery<T> setOrder (JpaCriteriaQuery<T> query, HibernateCriteriaBuilder cb,
                                             JpaRoot<JpaUserEntity> user, LinkedHashMap<String, OrderTypeCriteria> orderFields){
        List<Order> orders = new ArrayList<>();
        orderFields.forEach((fieldName, orderType) -> orders.add(orderType == OrderTypeCriteria.ASC
                ? cb.asc(user.get(fieldName))
                : cb.desc(user.get(fieldName))));
        return query.orderBy(orders);
    }

    private HibernateCriteriaBuilder criteriaBuilder() {
        return entityManager.unwrap(Session.class).getCriteriaBuilder();
    }

    private <T> JpaJoin<JpaUserEntity, T> jpaEntityLeftJoin(Class<T> jpaEntityClass, String fieldName, JpaRoot<JpaUserEntity> user, HibernateCriteriaBuilder cb) {
        JpaEntityJoin<JpaUserEntity, T> entity =
                user.join(jpaEntityClass, JoinType.LEFT);
        return entity.on(cb.equal(entity.get("id"), user.get(fieldName)));
    }

    private <T> BaseQuery<T> base(Class<T> projectionClass) {
        HibernateCriteriaBuilder cb = criteriaBuilder();
        JpaCriteriaQuery<T> query = cb.createQuery(projectionClass);
        JpaRoot<JpaUserEntity> user = query.from(JpaUserEntity.class);

        return new BaseQuery<>(cb, query, user);
    }

    public record BaseQuery<T> (
            HibernateCriteriaBuilder cb,
            JpaCriteriaQuery<T> query,
            JpaRoot<JpaUserEntity> user
    ){
    }
}
