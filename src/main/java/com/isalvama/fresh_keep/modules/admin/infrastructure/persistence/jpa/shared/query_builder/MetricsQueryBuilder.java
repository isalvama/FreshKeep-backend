package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.query_builder;

import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.projection.MetricProjection;
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

    public <R> JpaCriteriaQuery<MetricProjection> build(Class<R> rootEntityClass, PredicateSpecification<R> filters, String dateFieldName) {
        HibernateCriteriaBuilder cb = criteriaBuilder();
        JpaCriteriaQuery<MetricProjection> query = cb.createQuery(MetricProjection.class);
        JpaRoot<R> root = query.from(rootEntityClass);

        Expression<LocalDate> day = cb.cast(root.get(dateFieldName), LocalDate.class);

        return query
                .select(cb.construct(MetricProjection.class, day, cb.count(root)))
                .where(restriction(filters, root, cb))
                .groupBy(day)
                .orderBy(cb.asc(day));
    }

    public <R> Predicate restriction(PredicateSpecification<R> filters, JpaRoot<R> root, HibernateCriteriaBuilder cb) {
        Predicate predicate = filters.toPredicate(root, cb);
        return predicate != null ? predicate : cb.conjunction();
    }

    private HibernateCriteriaBuilder criteriaBuilder() {
        return entityManager.unwrap(Session.class).getCriteriaBuilder();
    }
}
