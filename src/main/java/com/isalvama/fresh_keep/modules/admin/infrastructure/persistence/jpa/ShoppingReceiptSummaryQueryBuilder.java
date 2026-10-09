package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductEntity;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaReceiptImageEntity;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaShoppingReceiptEntity;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.projection.ReceiptDetailProjection;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.projection.ReceiptSummaryProjection;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.entity.JpaUserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class ShoppingReceiptSummaryQueryBuilder {
    private final EntityManager entityManager;

    public JpaCriteriaQuery<ReceiptSummaryProjection> buildReceiptSummaryProjectionQuery (PredicateSpecification<JpaShoppingReceiptEntity> filters){

        HibernateCriteriaBuilder cb = criteriaBuilder();
        JpaCriteriaQuery<ReceiptSummaryProjection> query = cb.createQuery(ReceiptSummaryProjection.class);
        JpaRoot<JpaShoppingReceiptEntity> receipt = query.from(JpaShoppingReceiptEntity.class);

        List<Selection<?>> baseSelection = new ArrayList<>(buildBaseFields(cb, receipt));

        baseSelection.add(nonDeletedProductCount(query, cb, receipt));

        query.select(cb.construct(ReceiptSummaryProjection.class, baseSelection.toArray(new Selection[0])));

        query.where(restriction(filters, receipt, cb));

        return query.orderBy(cb.desc(receipt.get("purchaseDate")),
                cb.desc(receipt.get("createdAt")),
                cb.asc(receipt.get("id")));
    }

    public JpaCriteriaQuery<ReceiptDetailProjection> buildReceiptDetailProjectionQuery (UUID receiptId){

        HibernateCriteriaBuilder cb = criteriaBuilder();
        JpaCriteriaQuery<ReceiptDetailProjection> query = cb.createQuery(ReceiptDetailProjection.class);
        JpaRoot<JpaShoppingReceiptEntity> receipt = query.from(JpaShoppingReceiptEntity.class);

        List<Selection<?>> selection = new ArrayList<>(buildBaseFields(cb, receipt));

        selection.addAll(receiptImageFields(cb, receipt));

        query.select(cb.construct(ReceiptDetailProjection.class, selection.toArray(new Selection[0])));

        return query.where((cb.equal(receipt.get("id"), receiptId)));
    }

    public JpaCriteriaQuery<Long> countQuery(PredicateSpecification<JpaShoppingReceiptEntity> filters) {
        HibernateCriteriaBuilder cb = criteriaBuilder();
        JpaCriteriaQuery<Long> query = cb.createQuery(Long.class);
        JpaRoot<JpaShoppingReceiptEntity> receipt = query.from(JpaShoppingReceiptEntity.class);

        return query.select(cb.count(receipt)).where(restriction(filters, receipt, cb));
    }



    private List<Selection<?>> buildBaseFields(HibernateCriteriaBuilder cb, JpaRoot<JpaShoppingReceiptEntity> receipt) {

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
                        receipt.get("createdAt"));
    }


    private List<Selection<?>> receiptImageFields(HibernateCriteriaBuilder cb, JpaRoot<JpaShoppingReceiptEntity> receipt) {

        JpaJoin<JpaShoppingReceiptEntity, JpaReceiptImageEntity> receiptImage = jpaEntityLeftJoin(JpaReceiptImageEntity.class, "receiptImageId", receipt, cb);

        return List.of(
                receiptImage.get("id"),
                receiptImage.get("assetId"),
                receiptImage.get("mimeType"));
    }


    private Subquery<Long> nonDeletedProductCount(JpaCriteriaQuery<?> query, HibernateCriteriaBuilder cb,
                                                         JpaRoot<JpaShoppingReceiptEntity> receipt) {
        Subquery<Long> productCount = query.subquery(Long.class);
        Root<JpaProductEntity> product = productCount.from(JpaProductEntity.class);

        return productCount.select(cb.count(product))
                .where(cb.equal(product.get("shoppingReceiptId"), receipt.get("id")));
    }

    private static Predicate restriction(PredicateSpecification<JpaShoppingReceiptEntity> filters,
                                         JpaRoot<JpaShoppingReceiptEntity> receipt, HibernateCriteriaBuilder cb) {
        Predicate predicate = filters.toPredicate(receipt, cb);
        return predicate != null ? predicate : cb.conjunction();
    }

    private HibernateCriteriaBuilder criteriaBuilder() {
        return entityManager.unwrap(Session.class).getCriteriaBuilder();
    }

    private <T> JpaJoin<JpaShoppingReceiptEntity, T> jpaEntityLeftJoin(Class<T> jpaEntityClass, String fieldName, JpaRoot<JpaShoppingReceiptEntity> receipt, HibernateCriteriaBuilder cb) {
        JpaEntityJoin<JpaShoppingReceiptEntity, T> entity =
                receipt.join(jpaEntityClass, JoinType.LEFT);
        return entity.on(cb.equal(entity.get("id"), receipt.get(fieldName)));
    }

}


