package com.oasis.FIFAFanWallet.service;

import com.oasis.FIFAFanWallet.dto.WalletRequest;
import com.oasis.FIFAFanWallet.dto.WalletResponse;
import com.oasis.FIFAFanWallet.enums.WalletStatus;
import com.oasis.FIFAFanWallet.exception.AccessDeniedException;
import com.oasis.FIFAFanWallet.exception.UserNotFoundException;
import com.oasis.FIFAFanWallet.exception.WalletAlreadyExistsException;
import com.oasis.FIFAFanWallet.exception.WalletNotFoundException;
import com.oasis.FIFAFanWallet.model.Wallet;
import com.oasis.FIFAFanWallet.model.auth.User;
import com.oasis.FIFAFanWallet.repo.UserRepository;
import com.oasis.FIFAFanWallet.repo.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepo;
    private final WalletRepository walletRepository;

    public List<WalletResponse> getUserWallets(UUID userId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserId(userId).orElseThrow(() -> new UserNotFoundException("User not found!"));
        if(!user.getEmail().equals(email)){
            throw new AccessDeniedException("User doesn't have access.");
        }
        List<Wallet> wallets = walletRepository.findAllByUser(user);

        return wallets.stream()
                .map(wallet -> new WalletResponse(wallet.getWalletId(), wallet.getBalance(), wallet.getCurrency()))
                .toList();
    }

    public WalletResponse createUserWallet(UUID userId, WalletRequest walletRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserId(userId).orElseThrow(() -> new UserNotFoundException("User not found!"));

        if(!user.getEmail().equals(email)){
            throw new AccessDeniedException("User doesn't have access.");
        }

        boolean walletExists = walletRepository.existsByUserAndCurrency(user, walletRequest.currency());
        if(walletExists){
            throw new WalletAlreadyExistsException("Wallet already exists for this currency");
        }
        Wallet wallet = new Wallet(walletRequest.currency(), BigDecimal.ZERO, user, WalletStatus.ACTIVE);
        Wallet savedWallet = walletRepository.save(wallet);
        return new WalletResponse(savedWallet.getWalletId(), savedWallet.getBalance(), savedWallet.getCurrency());
    }

    public void disableUserWallet(UUID userId, UUID walletId) {
        Wallet wallet = walletRepository.findByWalletIdAndUser_UserId(walletId, userId).orElseThrow(() -> new WalletNotFoundException("Wallet not found."));
        wallet.setStatus(WalletStatus.DISABLED);
        walletRepository.save(wallet);
    }
}
