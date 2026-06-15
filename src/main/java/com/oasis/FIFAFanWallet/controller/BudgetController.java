package com.oasis.FIFAFanWallet.controller;

import com.oasis.FIFAFanWallet.dto.BudgetRequest;
import com.oasis.FIFAFanWallet.dto.BudgetResponse;
import com.oasis.FIFAFanWallet.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;

    @PostMapping
    ResponseEntity<BudgetResponse> createBudget(@RequestBody BudgetRequest budgetRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.createBudget(budgetRequest));
    }

}
