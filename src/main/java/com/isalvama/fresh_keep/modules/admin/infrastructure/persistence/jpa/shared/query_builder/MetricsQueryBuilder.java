package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.query_builder;

import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.projection.MetricProjection;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaShoppingReceiptEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class MetricsQueryBuilder {
    private final EntityManager entityManager;

    public JpaCriteriaQuery<MetricProjection> build (PredicateSpecification<JpaShoppingReceiptEntity> filters, String dateFieldName){
        HibernateCriteriaBuilder cb = criteriaBuilder();
        JpaCriteriaQuery<MetricProjection> query = cb.createQuery(MetricProjection.class);
        JpaRoot<JpaShoppingReceiptEntity> receipt = query.from(JpaShoppingReceiptEntity.class);

        Expression<LocalDate> createdDay = cb.cast(receipt.get(dateFieldName), LocalDate.class);

        return query
                .select(cb.construct(MetricProjection.class, createdDay, cb.count(receipt)))
                .where(restriction(filters, receipt, cb))
                .groupBy(createdDay)
                .orderBy(cb.asc(createdDay));
    }

    public Predicate restriction(PredicateSpecification<JpaShoppingReceiptEntity> filters,
                                 JpaRoot<JpaShoppingReceiptEntity> receipt, HibernateCriteriaBuilder cb) {
        Predicate predicate = filters.toPredicate(receipt, cb);
        return predicate != null ? predicate : cb.conjunction();
    }

    private HibernateCriteriaBuilder criteriaBuilder() {
        return entityManager.unwrap(Session.class).getCriteriaBuilder();
    }
}
