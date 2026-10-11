package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shopping_receipt_query.query_builder;

import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.projection.MetricProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.OrderTypeCriteria;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductEntity;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaReceiptImageEntity;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaShoppingReceiptEntity;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shopping_receipt_query.projection.ReceiptDetailProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shopping_receipt_query.projection.ReceiptSummaryProjection;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.entity.JpaUserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;


@Component
@RequiredArgsConstructor
public class ShoppingReceiptQueryBuilder {
    private final EntityManager entityManager;

    public JpaCriteriaQuery<ReceiptSummaryProjection> buildReceiptSummaryProjectionQuery (PredicateSpecification<JpaShoppingReceiptEntity> filters){

        BaseQuery<ReceiptSummaryProjection> base = base(ReceiptSummaryProjection.class);

        List<Selection<?>> baseSelection = new ArrayList<>(buildBaseSelection(base.cb(), base.receipt()));
        baseSelection.add(nonDeletedProductCount(base.query(), base.cb(), base.receipt()));

        JpaCriteriaQuery<ReceiptSummaryProjection> query = base.query().select(base.cb().construct(ReceiptSummaryProjection.class, baseSelection.toArray(new Selection[0])));

        query.where(restriction(filters, base.receipt(), base.cb()));

        LinkedHashMap<String, OrderTypeCriteria> order = new LinkedHashMap<>();
        order.put("purchaseDate", OrderTypeCriteria.DESC);
        order.put("createdAt", OrderTypeCriteria.DESC);
        order.put("id", OrderTypeCriteria.ASC);

        return setOrder(query, base.cb(), base.receipt(), order);
    }

    public JpaCriteriaQuery<ReceiptDetailProjection> buildReceiptDetailProjectionQuery (UUID receiptId){

        BaseQuery<ReceiptDetailProjection> base = base(ReceiptDetailProjection.class);

        List<Selection<?>> selection = new ArrayList<>(buildBaseSelection(base.cb(), base.receipt()));
        selection.addAll(receiptImageFieldsSelection(base.cb(), base.receipt()));

        JpaCriteriaQuery<ReceiptDetailProjection> query = base.query().select(base.cb().construct(ReceiptDetailProjection.class, selection.toArray(new Selection[0])));
        return query.where((base.cb().equal(base.receipt().get("id"), receiptId)));
    }

    public JpaCriteriaQuery<Long> countQuery(PredicateSpecification<JpaShoppingReceiptEntity> filters) {
        BaseQuery<Long> base = base(Long.class);
        return base.query().select(base.cb().count(base.receipt())).where(restriction(filters, base.receipt(), base.cb()));
    }

    public List<Selection<?>> buildBaseSelection(HibernateCriteriaBuilder cb, JpaRoot<JpaShoppingReceiptEntity> receipt) {
        JpaJoin<JpaShoppingReceiptEntity, JpaUserEntity> creator = jpaEntityLeftJoin(JpaUserEntity.class, "creatorId", receipt, cb);
        JpaJoin<JpaShoppingReceiptEntity, JpaSpaceEntity> space = jpaEntityLeftJoin(JpaSpaceEntity.class, "spaceId", receipt, cb);

        return List.of(
                receipt.get("id"),
                receipt.get("creatorId"),
                creator.get("email"),
                creator.get("userName"),
                receipt.get("spaceId"),
                space.get("name"),
                receipt.get("storeName"),
                receipt.get("purchaseDate"),
                receipt.get("createdAt")
        );
    }

    public List<Selection<?>> receiptImageFieldsSelection(HibernateCriteriaBuilder cb, JpaRoot<JpaShoppingReceiptEntity> receipt) {
        JpaJoin<JpaShoppingReceiptEntity, JpaReceiptImageEntity> receiptImage = jpaEntityLeftJoin(JpaReceiptImageEntity.class, "receiptImageId", receipt, cb);

        return List.of(
                receiptImage.get("id"),
                receiptImage.get("assetId"),
                receiptImage.get("mimeType"));
    }


    public Subquery<Long> nonDeletedProductCount(JpaCriteriaQuery<?> query, HibernateCriteriaBuilder cb,
                                                         JpaRoot<JpaShoppingReceiptEntity> receipt) {
        Subquery<Long> productCount = query.subquery(Long.class);
        Root<JpaProductEntity> product = productCount.from(JpaProductEntity.class);

        return productCount.select(cb.count(product))
                .where(cb.equal(product.get("shoppingReceiptId"), receipt.get("id")));
    }

    public Predicate restriction(PredicateSpecification<JpaShoppingReceiptEntity> filters,
                                         JpaRoot<JpaShoppingReceiptEntity> receipt, HibernateCriteriaBuilder cb) {
        Predicate predicate = filters.toPredicate(receipt, cb);
        return predicate != null ? predicate : cb.conjunction();
    }

    public <T> JpaCriteriaQuery<T> setOrder (JpaCriteriaQuery<T> query, HibernateCriteriaBuilder cb,
                                             JpaRoot<JpaShoppingReceiptEntity> receipt, LinkedHashMap<String, OrderTypeCriteria> orderFields){
        List<Order> orders = new ArrayList<>();
        orderFields.forEach((fieldName, orderType) -> orders.add(orderType == OrderTypeCriteria.ASC
                ? cb.asc(receipt.get(fieldName))
                : cb.desc(receipt.get(fieldName))));
        return query.orderBy(orders);
    }

    private HibernateCriteriaBuilder criteriaBuilder() {
        return entityManager.unwrap(Session.class).getCriteriaBuilder();
    }

    private <T> JpaJoin<JpaShoppingReceiptEntity, T> jpaEntityLeftJoin(Class<T> jpaEntityClass, String fieldName, JpaRoot<JpaShoppingReceiptEntity> receipt, HibernateCriteriaBuilder cb) {
        JpaEntityJoin<JpaShoppingReceiptEntity, T> entity =
                receipt.join(jpaEntityClass, JoinType.LEFT);
        return entity.on(cb.equal(entity.get("id"), receipt.get(fieldName)));
    }

    private <T> BaseQuery<T> base(Class<T> projectionClass) {
        HibernateCriteriaBuilder cb = criteriaBuilder();
        JpaCriteriaQuery<T> query = cb.createQuery(projectionClass);
        JpaRoot<JpaShoppingReceiptEntity> receipt = query.from(JpaShoppingReceiptEntity.class);

        return new BaseQuery<>(cb, query, receipt);
    }

    public record BaseQuery<T> (
            HibernateCriteriaBuilder cb,
            JpaCriteriaQuery<T> query,
            JpaRoot<JpaShoppingReceiptEntity> receipt
    ){
    }

}


