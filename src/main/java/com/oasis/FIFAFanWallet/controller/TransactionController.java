package com.oasis.FIFAFanWallet.controller;

import com.oasis.FIFAFanWallet.dto.ExchangeResponse;
import com.oasis.FIFAFanWallet.dto.TransactionRequest;
import com.oasis.FIFAFanWallet.dto.TransactionResponse;
import com.oasis.FIFAFanWallet.dto.TransferResponse;
import com.oasis.FIFAFanWallet.enums.TransactionType;
import com.oasis.FIFAFanWallet.model.Transaction;
import com.oasis.FIFAFanWallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/transfers/{senderId}/{receiverId}")
    public ResponseEntity<TransferResponse> transfer(@PathVariable UUID senderId, @PathVariable UUID receiverId, @RequestBody TransactionRequest transactionRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.transfer(senderId, receiverId, transactionRequest));
    }

    @PostMapping("/wallets/exchange/{fromWalletId}/{toWalletId}")
    public ResponseEntity<ExchangeResponse> exchange(@PathVariable UUID fromWalletId, @PathVariable UUID toWalletId, @RequestBody TransactionRequest transactionRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.exchange(fromWalletId, toWalletId, transactionRequest));
    }

    @GetMapping("/user/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactionsUser(@RequestParam(required = false)TransactionType type){
        return ResponseEntity.ok(transactionService.getAllTransactionUser(type));
    }
}
