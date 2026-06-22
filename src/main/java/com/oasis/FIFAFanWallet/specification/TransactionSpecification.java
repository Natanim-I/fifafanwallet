package com.oasis.FIFAFanWallet.specification;

import com.oasis.FIFAFanWallet.enums.Currency;
import com.oasis.FIFAFanWallet.enums.TransactionType;
import com.oasis.FIFAFanWallet.model.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionSpecification {
    public static Specification<Transaction> filter(
            UUID userId,
            TransactionType type,
            Currency currency,
            LocalDateTime start,
            LocalDateTime end,
            BigDecimal min,
            BigDecimal max,
            BigDecimal exact
    ) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            p.add(cb.equal(root.get("wallet").get("user").get("userId"), userId));

            if (type != null)
                p.add(cb.equal(root.get("type"), type));

            if (currency != null)
                p.add(cb.equal(root.get("wallet").get("currency"), currency));

            if (start != null)
                p.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));

            if (end != null)
                p.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));

            if (min != null)
                p.add(cb.greaterThanOrEqualTo(root.get("amount"), min));

            if (max != null)
                p.add(cb.lessThanOrEqualTo(root.get("amount"), max));

            if (exact != null)
                p.add(cb.equal(root.get("amount"), exact));

            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
