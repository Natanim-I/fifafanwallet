package com.oasis.FIFAFanWallet.repo;

import com.oasis.FIFAFanWallet.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    Optional<Budget> findByBudgetIdAndUserId(UUID budgetId, UUID userId);
}
