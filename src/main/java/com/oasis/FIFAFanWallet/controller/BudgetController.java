package com.oasis.FIFAFanWallet.controller;

import com.oasis.FIFAFanWallet.dto.BudgetRequest;
import com.oasis.FIFAFanWallet.dto.BudgetResponse;
import com.oasis.FIFAFanWallet.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/budget")
public class BudgetController {
    private final BudgetService budgetService;

    @PostMapping("/create")
    ResponseEntity<BudgetResponse> createBudget(@RequestBody BudgetRequest budgetRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.createBudget(budgetRequest));
    }

    @GetMapping("/{budgetId}/details")
    ResponseEntity<BudgetResponse> getBudgetDetails(@PathVariable UUID budgetId){
        return ResponseEntity.ok(budgetService.getBudgetDetails(budgetId));
    }

    @PutMapping("{budgetId}/update")
    ResponseEntity<BudgetResponse> updateBudget(@PathVariable UUID budgetId, @RequestBody BudgetRequest budgetRequest){
        return ResponseEntity.ok(budgetService.updateBudget(budgetId, budgetRequest));
    }

}
