package com.oasis.FIFAFanWallet.controller;

import com.oasis.FIFAFanWallet.dto.WalletRequest;
import com.oasis.FIFAFanWallet.dto.WalletResponse;
import com.oasis.FIFAFanWallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin()
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/total-balance")
    public ResponseEntity<BigDecimal> calculateTotalBalance(){
        return ResponseEntity.ok(walletService.calculateTotalBalance());
    }

    @GetMapping("/wallets")
    public ResponseEntity<List<WalletResponse>> getUserWallets() {
        return ResponseEntity.ok(walletService.getUserWallets());
    }

    @PostMapping("/wallet/create")
    public ResponseEntity<WalletResponse> createWallet(@RequestBody WalletRequest walletRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.createUserWallet(walletRequest));
    }

    @DeleteMapping("/wallet/{walletId}/disable")
    public ResponseEntity<Void> disableUserWallet(@PathVariable UUID walletId){
        walletService.disableUserWallet(walletId);
        return ResponseEntity.noContent().build();
    }
}
