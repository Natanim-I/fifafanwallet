package com.oasis.FIFAFanWallet.service;

import com.oasis.FIFAFanWallet.dto.BudgetRequest;
import com.oasis.FIFAFanWallet.dto.BudgetResponse;
import com.oasis.FIFAFanWallet.exception.BudgetNotFoundException;
import com.oasis.FIFAFanWallet.exception.IllegalArgumentException;
import com.oasis.FIFAFanWallet.exception.UserNotFoundException;
import com.oasis.FIFAFanWallet.model.Budget;
import com.oasis.FIFAFanWallet.model.auth.User;
import com.oasis.FIFAFanWallet.repo.BudgetRepository;
import com.oasis.FIFAFanWallet.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;

    public List<BudgetResponse> getAllBudgets() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
        List<Budget> budgets = budgetRepository.findAllByUserId(user.getUserId()).orElseThrow(() -> new BudgetNotFoundException("Budget associated with this user not found."));

        return budgets.stream()
                .map(budget -> new BudgetResponse(
                        budget.getBudgetId(),
                        budget.getLimitAmount(),
                        budget.getSpentAmount(),
                        budget.getType(),
                        budget.getStartDate(),
                        budget.getEndDate()
                )).toList();
    }

    public BudgetResponse getBudget(UUID budgetId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));
        Budget budget = budgetRepository.findByBudgetIdAndUserId(budgetId, user.getUserId()).orElseThrow(() -> new BudgetNotFoundException("Budget associated with this user not found."));

        return new BudgetResponse(
                budget.getBudgetId(),
                budget.getLimitAmount(),
                budget.getSpentAmount(),
                budget.getType(),
                budget.getStartDate(),
                budget.getEndDate()
        );
    }

    @Transactional
    public BudgetResponse createBudget(BudgetRequest budgetRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));

        if (budgetRequest.startDate().isAfter(budgetRequest.endDate())) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }

        Budget budget = new Budget();
        budget.setUserId(user.getUserId());
        budget.setLimitAmount(budgetRequest.limitAmount());
        budget.setSpentAmount(BigDecimal.ZERO);

        validateBudgetDates(budgetRequest);

        budget.setType(budgetRequest.type());
        budget.setStartDate(budgetRequest.startDate());
        budget.setEndDate(budgetRequest.endDate());

        return new BudgetResponse(
                budget.getBudgetId(),
                budget.getLimitAmount(),
                budget.getSpentAmount(),
                budget.getType(),
                budget.getStartDate(),
                budget.getEndDate()
        );
    }

    public BudgetResponse getBudgetDetails(UUID budgetId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));
        Budget budget = budgetRepository.findByBudgetIdAndUserId(budgetId, user.getUserId()).orElseThrow(() -> new BudgetNotFoundException("Budget associated with this user not found.."));

        return new BudgetResponse(
                budget.getBudgetId(),
                budget.getLimitAmount(),
                budget.getSpentAmount(),
                budget.getType(),
                budget.getStartDate(),
                budget.getEndDate()
        );
    }

    @Transactional
    public BudgetResponse updateBudget(UUID budgetId, BudgetRequest budgetRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));

        Budget budget = budgetRepository.findByBudgetIdAndUserId(budgetId, user.getUserId()).orElseThrow(() -> new BudgetNotFoundException("Budget not found."));
        budget.setLimitAmount(budgetRequest.limitAmount());

        if(budget.getStartDate().isAfter(LocalDateTime.now())){
            validateBudgetDates(budgetRequest);
            budget.setType(budgetRequest.type());
            budget.setStartDate(budgetRequest.startDate());
            budget.setEndDate(budgetRequest.endDate());
        }

        return new BudgetResponse(
                budget.getBudgetId(),
                budget.getLimitAmount(),
                budget.getSpentAmount(),
                budget.getType(),
                budget.getStartDate(),
                budget.getEndDate()
        );
    }

    private void validateBudgetDates(BudgetRequest budgetRequest) {

        LocalDate start = budgetRequest.startDate().toLocalDate();
        LocalDate end = budgetRequest.endDate().toLocalDate();

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "Start date must be before end date.");
        }

        switch (budgetRequest.type()) {

            case WEEKLY -> {
                if (!end.equals(start.plusDays(7))) {
                    throw new IllegalArgumentException(
                            "Weekly budgets must span exactly 7 days.");
                }
            }

            case BIWEEKLY -> {
                if (!end.equals(start.plusDays(14))) {
                    throw new IllegalArgumentException(
                            "Bi-weekly budgets must span exactly 14 days.");
                }
            }

            case MONTHLY -> {
                if (!end.equals(start.plusMonths(1))) {
                    throw new IllegalArgumentException(
                            "Monthly budgets must span exactly 1 month.");
                }
            }

            case TRIP -> {
            }
        }
    }

    public void deleteBudget(UUID budgetId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));
        budgetRepository.findByBudgetIdAndUserId(budgetId, user.getUserId()).orElseThrow(() -> new BudgetNotFoundException("Budget not found."));
    }

}
