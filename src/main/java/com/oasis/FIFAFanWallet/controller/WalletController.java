package com.oasis.FIFAFanWallet.controller;

import com.oasis.FIFAFanWallet.dto.WalletRequest;
import com.oasis.FIFAFanWallet.dto.WalletResponse;
import com.oasis.FIFAFanWallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/users/{userId}/wallets")
    public ResponseEntity<List<WalletResponse>> getUserWallets(@PathVariable UUID userId) {
        return ResponseEntity.ok(walletService.getUserWallets(userId));
    }

    @PostMapping("/users/{userId}/wallet")
    public ResponseEntity<WalletResponse> createWallet(@PathVariable UUID userId, @RequestBody WalletRequest walletRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.createUserWallet(userId, walletRequest));
    }

    @DeleteMapping("/users/{userId}/wallets/{walletId}")
    public ResponseEntity<Void> disableUserWallet(@PathVariable UUID userId, @PathVariable UUID walletId){
        walletService.disableUserWallet(userId, walletId);
        return ResponseEntity.noContent().build();
    }
}
