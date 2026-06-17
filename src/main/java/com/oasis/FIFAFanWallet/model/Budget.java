package com.oasis.FIFAFanWallet.model;

import com.oasis.FIFAFanWallet.enums.BudgetCategory;
import com.oasis.FIFAFanWallet.enums.BudgetPeriod;
import com.oasis.FIFAFanWallet.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID budgetId;
    private UUID userId;
    @Enumerated(EnumType.STRING)
    private Currency currency;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    @Enumerated(EnumType.STRING)
    private BudgetPeriod type;
    @Enumerated(EnumType.STRING)
    private BudgetCategory category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
