package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.specification;

import org.springframework.data.jpa.domain.PredicateSpecification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

public final class SpecificationBuilder {

    private SpecificationBuilder() {
    }

    public static <T> PredicateSpecification<T> matchingFromToFilters(LocalDate from, LocalDate to) {
        return PredicateSpecification.allOf(
                purchasedOnOrAfter(from),
                purchasedOnOrBefore(to));
    }

    public static <T> PredicateSpecification<T> matchingFilters(
            LocalDate from, LocalDate to, UUID spaceId, UUID creatorId) {
        return PredicateSpecification.allOf(
                purchasedOnOrAfter(from),
                purchasedOnOrBefore(to),
                inSpace(spaceId),
                createdBy(creatorId));
    }

    public static <T>  PredicateSpecification<T> purchasedOnOrAfter(LocalDate from) {
        if (from == null) return PredicateSpecification.unrestricted();
        return (receipt, cb) -> cb.greaterThanOrEqualTo(receipt.get("purchaseDate"), startOfDay(from));
    }

    /** [to] is inclusive, so the cut-off is the start of the following day. */
    public static <T> PredicateSpecification<T> purchasedOnOrBefore(LocalDate to) {
        if (to == null) return PredicateSpecification.unrestricted();
        return (receipt, cb) -> cb.lessThan(receipt.get("purchaseDate"), startOfDay(to.plusDays(1)));
    }

    public static <T> PredicateSpecification<T> inSpace(UUID spaceId) {
        if (spaceId == null) return PredicateSpecification.unrestricted();
        return (receipt, cb) -> cb.equal(receipt.get("spaceId"), spaceId);
    }

    public static <T> PredicateSpecification<T> createdBy(UUID creatorId) {
        if (creatorId == null) return PredicateSpecification.unrestricted();
        return (receipt, cb) -> cb.equal(receipt.get("creatorId"), creatorId);
    }

    private static Instant startOfDay(LocalDate day) {
        return day.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }
}
