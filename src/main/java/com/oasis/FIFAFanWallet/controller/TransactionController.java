package com.oasis.FIFAFanWallet.controller;

import com.oasis.FIFAFanWallet.dto.ExchangeResponse;
import com.oasis.FIFAFanWallet.dto.TransactionRequest;
import com.oasis.FIFAFanWallet.dto.TransactionResponse;
import com.oasis.FIFAFanWallet.dto.TransferResponse;
import com.oasis.FIFAFanWallet.enums.Currency;
import com.oasis.FIFAFanWallet.enums.TransactionType;
import com.oasis.FIFAFanWallet.model.Transaction;
import com.oasis.FIFAFanWallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/wallet/{walletId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(@PathVariable UUID walletId, @RequestBody TransactionRequest transactionRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.deposit(walletId, transactionRequest));
    }

    @PostMapping("/wallet/{walletId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@PathVariable UUID walletId, @RequestBody TransactionRequest transactionRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.withdraw(walletId, transactionRequest));
    }

    @PostMapping("/wallet/transfer/{senderId}/{receiverId}")
    public ResponseEntity<TransferResponse> transfer(@PathVariable UUID senderId, @PathVariable UUID receiverId, @RequestBody TransactionRequest transactionRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.transfer(senderId, receiverId, transactionRequest));
    }

    @PostMapping("/wallet/exchange/{fromWalletId}/{toWalletId}")
    public ResponseEntity<ExchangeResponse> exchange(@PathVariable UUID fromWalletId, @PathVariable UUID toWalletId, @RequestBody TransactionRequest transactionRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.exchange(fromWalletId, toWalletId, transactionRequest));
    }

    @GetMapping("/user/transactions")
    public ResponseEntity<List<TransactionResponse>> getAllTransactionsUser(
            @RequestParam(required = false)TransactionType type,
            @RequestParam(required = false) Currency currency,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) BigDecimal amount){
        return ResponseEntity.ok(transactionService.getAllTransactionUser(type, currency, startDate, endDate, minAmount, maxAmount, amount));
    }
}
