package com.oasis.FIFAFanWallet.repo;

import com.oasis.FIFAFanWallet.enums.BudgetCategory;
import com.oasis.FIFAFanWallet.enums.BudgetPeriod;
import com.oasis.FIFAFanWallet.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    Optional<Budget> findByBudgetIdAndUserId(UUID budgetId, UUID userId);

    Optional<List<Budget>> findAllByUserId(UUID userId);

    @Query("""
        SELECT b from Budget b
        WHERE b.userId = :userId
        AND b.category = :category
        AND b.startDate <= :paymentTime
        AND b.endDate >= :paymentTime
    """)
    Optional<Budget> findBudgets(
            @Param("userId") UUID userId,
            @Param("category") BudgetCategory category,
            @Param("paymentTime") LocalDateTime paymentTime
            );

    @Query("""
        SELECT COUNT(b) > 0
        FROM Budget b
        WHERE b.userId = :userId
        AND b.category = :category
        AND b.type = :type
        AND b.startDate <= :endDate
        AND b.endDate >= :startDate
        """)
    boolean existsOverlappingBudget(
            UUID userId,
            BudgetCategory category,
            BudgetPeriod type,
            LocalDateTime localDateTime,
            LocalDateTime localDateTime1);
}
