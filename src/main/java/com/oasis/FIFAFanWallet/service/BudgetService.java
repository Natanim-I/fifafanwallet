package com.oasis.FIFAFanWallet.service;

import com.oasis.FIFAFanWallet.dto.BudgetRequest;
import com.oasis.FIFAFanWallet.dto.BudgetResponse;
import com.oasis.FIFAFanWallet.exception.UserNotFoundException;
import com.oasis.FIFAFanWallet.model.Budget;
import com.oasis.FIFAFanWallet.model.auth.User;
import com.oasis.FIFAFanWallet.repo.BudgetRepository;
import com.oasis.FIFAFanWallet.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;

    public BudgetResponse createBudget(BudgetRequest budgetRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));

        Budget budget = new Budget();
        budget.setBudgetId(user.getUserId());
        budget.setLimitAmount(budgetRequest.limitAmount());
        budget.setType(budgetRequest.type());
        budget.setSpentAmount(BigDecimal.ZERO);
        budget.setStartDate(budgetRequest.startDate());
        budget.setEndDate(budgetRequest.endDate());

        Budget savedBudget = budgetRepository.save(budget);

        return new BudgetResponse(
                savedBudget.getBudgetId(),
                savedBudget.getLimitAmount(),
                savedBudget.getSpentAmount(),
                savedBudget.getType(),
                savedBudget.getStartDate(),
                savedBudget.getEndDate()
        );
    }
}
