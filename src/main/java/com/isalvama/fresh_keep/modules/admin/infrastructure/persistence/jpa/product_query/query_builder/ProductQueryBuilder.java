package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.query_builder;

import com.isalvama.fresh_keep.modules.admin.domain.criteria.OrderType;
import com.isalvama.fresh_keep.modules.admin.domain.criteria.ProductSortType;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.projection.ProductDetailsProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.projection.ProductSummaryProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.projection.ProductTypeCountProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.specification.SpecificationBuilder;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductEntity;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaShoppingReceiptEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaStorageSpotEntity;
import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.entity.JpaUserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Selection;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductQueryBuilder {
    private final EntityManager entityManager;

    public JpaCriteriaQuery<ProductSummaryProjection> buildProductSummaryProjectionQuery(
            PredicateSpecification<JpaProductEntity> filters, UUID creatorId, ProductSortType sortType) {
        BaseQuery<ProductSummaryProjection> base = base(ProductSummaryProjection.class);

        JpaEntityJoin<JpaProductEntity, JpaShoppingReceiptEntity> receipt = base.product().join(JpaShoppingReceiptEntity.class, JoinType.LEFT);
        receipt.on(base.cb().equal(receipt.get("id"), base.product().get("shoppingReceiptId")));
        Predicate creatorPredicate = creatorId == null ? base.cb().conjunction() : base.cb().equal(receipt.get("creatorId"), creatorId);

        List<Selection<?>> selection = new ArrayList<>(buildBaseSelection(base.cb(), base.product()));

        return base.query()
                .select(base.cb().construct(ProductSummaryProjection.class, selection.toArray(new Selection[0])))
                .where(base.cb().and(restriction(filters, base.product(), base.cb()), creatorPredicate))
                .orderBy(sortOrder(base.cb(), base.product(), sortType), base.cb().asc(base.product().get("id")));
    }

    public JpaCriteriaQuery<ProductSummaryProjection> buildProductsBySpaceQuery(UUID spaceId) {
        BaseQuery<ProductSummaryProjection> base = base(ProductSummaryProjection.class);
        HibernateCriteriaBuilder cb = base.cb();
        JpaRoot<JpaProductEntity> product = base.product();

        JpaJoin<JpaProductEntity, JpaStorageSpotEntity> storageSpot = storageSpotJoin(product, cb);

        List<Selection<?>> selection = new ArrayList<>(buildBaseSelection(cb, product));

        return base.query()
                .select(cb.construct(ProductSummaryProjection.class, selection.toArray(new Selection[0])))
                .where(cb.equal(storageSpot.get("space").get("id"), spaceId))
                .orderBy(cb.asc(product.get("expirationDate")), cb.asc(product.get("name")));
    }

    public JpaCriteriaQuery<ProductDetailsProjection> buildProductDetailProjectionQuery(UUID productId) {
        BaseQuery<ProductDetailsProjection> base = base(ProductDetailsProjection.class);
        HibernateCriteriaBuilder cb = base.cb();
        JpaRoot<JpaProductEntity> product = base.product();

        List<Selection<?>> selection = new ArrayList<>(buildBaseSelection(cb, product));
        selection.addAll(productDetailSelectionAddition(cb, product));

        return base.query().select(cb.construct(ProductDetailsProjection.class, selection.toArray(new Selection[0])))
                .where(cb.equal(product.get("id"), productId));
    }

    public JpaCriteriaQuery<ProductTypeCountProjection> buildProductTypeCountQuery() {
        BaseQuery<ProductTypeCountProjection> base = base(ProductTypeCountProjection.class);
        HibernateCriteriaBuilder cb = base.cb();
        JpaRoot<JpaProductEntity> product = base.product();
        Expression<Long> count = cb.count(product);

        return base.query()
                .select(cb.construct(ProductTypeCountProjection.class, product.get("productType"), count))
                .groupBy(product.get("productType"))
                .orderBy(cb.desc(count), cb.asc(product.get("productType")));
    }

    public List<Selection<?>> buildBaseSelection(HibernateCriteriaBuilder cb, JpaRoot<JpaProductEntity> product) {
        return List.of(
                product.get("id"),
                product.get("name"),
                product.get("expirationDate"),
                product.get("actualStorageSpotId"),
                product.get("productType"),
                product.get("shoppingReceiptId"),
                product.get("price"),
                product.get("currency")
        );
    }

    public List<Selection<?>> productDetailSelectionAddition(HibernateCriteriaBuilder cb, JpaRoot<JpaProductEntity> product) {
        JpaEntityJoin<JpaProductEntity, JpaShoppingReceiptEntity> receipt = product.join(JpaShoppingReceiptEntity.class, JoinType.LEFT);
        receipt.on(cb.equal(receipt.get("id"), product.get("shoppingReceiptId")));

        JpaEntityJoin<JpaShoppingReceiptEntity, JpaUserEntity> creator = receipt.join(JpaUserEntity.class, JoinType.LEFT);
        creator.on(cb.equal(creator.get("id"), receipt.get("creatorId")));

        JpaEntityJoin<JpaShoppingReceiptEntity, JpaSpaceEntity> space = receipt.join(JpaSpaceEntity.class, JoinType.LEFT);
        space.on(cb.equal(space.get("id"), receipt.get("spaceId")));

        JpaJoin<JpaProductEntity, JpaStorageSpotEntity> storageSpot = storageSpotJoin(product, cb);

        return List.of(
                storageSpot.get("type"),
                receipt.get("creatorId"),
                creator.get("userName"),
                creator.get("email"),
                receipt.get("spaceId"),
                space.get("name"),
                receipt.get("storeName"),
                receipt.get("purchaseDate"),
                product.get("createdAt")
        );
    }

    public Predicate restriction(PredicateSpecification<JpaProductEntity> filters,
                                 JpaRoot<JpaProductEntity> product, HibernateCriteriaBuilder cb) {
        Predicate predicate = filters.toPredicate(product, cb);
        return predicate != null ? predicate : cb.conjunction();
    }

    public PredicateSpecification<JpaProductEntity> matchingReceiptFilters(LocalDate from, LocalDate to, UUID spaceId, UUID creatorId) {
        return (productFrom, cb) -> {
            JpaFrom<?, JpaProductEntity> product = (JpaFrom<?, JpaProductEntity>) productFrom;
            JpaEntityJoin<JpaProductEntity, JpaShoppingReceiptEntity> receipt = product.join(JpaShoppingReceiptEntity.class, JoinType.LEFT);
            receipt.on(cb.equal(receipt.get("id"), product.get("shoppingReceiptId")));

            PredicateSpecification<JpaShoppingReceiptEntity> receiptFilters =
                    SpecificationBuilder.matchingFilters(from, to, spaceId, creatorId);
            return receiptFilters.toPredicate(receipt, cb);
        };
    }

    private JpaJoin<JpaProductEntity, JpaStorageSpotEntity> storageSpotJoin(JpaRoot<JpaProductEntity> product, HibernateCriteriaBuilder cb) {
        JpaEntityJoin<JpaProductEntity, JpaStorageSpotEntity> storageSpot = product.join(JpaStorageSpotEntity.class, JoinType.INNER);
        return storageSpot.on(cb.equal(storageSpot.get("id"), product.get("actualStorageSpotId")));
    }

    private Order sortOrder(HibernateCriteriaBuilder cb, JpaRoot<JpaProductEntity> product, ProductSortType sortType) {
        String attribute = switch (sortType) {
            case NAME_ASC, NAME_DESC -> "name";
            case EXPIRATION_DATE_ASC, EXPIRATION_DATE_DESC -> "expirationDate";
            case PRICE_ASC, PRICE_DESC -> "price";
        };
        return sortType.orderType() == OrderType.ASC ? cb.asc(product.get(attribute)) : cb.desc(product.get(attribute));
    }

    private HibernateCriteriaBuilder criteriaBuilder() {
        return entityManager.unwrap(Session.class).getCriteriaBuilder();
    }

    private <T> BaseQuery<T> base(Class<T> projectionClass) {
        HibernateCriteriaBuilder cb = criteriaBuilder();
        JpaCriteriaQuery<T> query = cb.createQuery(projectionClass);
        JpaRoot<JpaProductEntity> product = query.from(JpaProductEntity.class);

        return new BaseQuery<>(cb, query, product);
    }

    public record BaseQuery<T> (
            HibernateCriteriaBuilder cb,
            JpaCriteriaQuery<T> query,
            JpaRoot<JpaProductEntity> product
    ){
    }
}
