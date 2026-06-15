package com.oasis.FIFAFanWallet.model;

import com.oasis.FIFAFanWallet.enums.BudgetCategory;
import com.oasis.FIFAFanWallet.enums.BudgetPeriod;
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
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    @Enumerated(EnumType.STRING)
    private BudgetPeriod type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
