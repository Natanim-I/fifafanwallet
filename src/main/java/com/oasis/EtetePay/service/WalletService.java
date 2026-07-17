package com.oasis.EtetePay.service;

import com.oasis.EtetePay.dto.WalletRequest;
import com.oasis.EtetePay.dto.WalletResponse;
import com.oasis.EtetePay.enums.Currency;
import com.oasis.EtetePay.enums.KYCStatus;
import com.oasis.EtetePay.enums.KycLevel;
import com.oasis.EtetePay.enums.WalletStatus;
import com.oasis.EtetePay.exception.*;
import com.oasis.EtetePay.exception.IllegalArgumentException;
import com.oasis.EtetePay.model.KYCProfile;
import com.oasis.EtetePay.model.Wallet;
import com.oasis.EtetePay.model.auth.User;
import com.oasis.EtetePay.repo.KycProfileRepository;
import com.oasis.EtetePay.repo.UserRepository;
import com.oasis.EtetePay.repo.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepo;
    private final WalletRepository walletRepository;
    private final ExchangeRateService exchangeRateService;
    private final KycProfileRepository kycProfileRepository;

    public BigDecimal calculateTotalBalance() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));

        List<Wallet> wallets = walletRepository.findAllByUser(user);

        BigDecimal totalBalance = BigDecimal.ZERO;

        for(Wallet wallet : wallets) {
            if (wallet.getCurrency() == Currency.USD)
                totalBalance = totalBalance.add(wallet.getBalance());
            else {
                BigDecimal rate = exchangeRateService.getCurrencyExchangeRate(wallet.getCurrency(), Currency.USD);
                totalBalance = totalBalance.add(wallet.getBalance().multiply(rate));
            }
        }
        return totalBalance.setScale(2, RoundingMode.HALF_UP);
    }

    public List<WalletResponse> getUserWallets() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found!"));

        List<Wallet> wallets = walletRepository.findAllByUser(user);

        return wallets.stream()
                .map(wallet -> new WalletResponse(wallet.getWalletId(), wallet.getBalance(), wallet.getCurrency()))
                .toList();
    }

    public WalletResponse createUserWallet(WalletRequest walletRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found!"));

        boolean walletExists = walletRepository.existsByUserAndCurrency(user, walletRequest.currency());
        if(walletExists){
            throw new WalletAlreadyExistsException("Wallet already exists for this currency");
        }
        Wallet wallet = new Wallet(walletRequest.currency(), BigDecimal.ZERO, user, WalletStatus.ACTIVE);
        Wallet savedWallet = walletRepository.save(wallet);
        return new WalletResponse(savedWallet.getWalletId(), savedWallet.getBalance(), savedWallet.getCurrency());
    }

    private void validateWalletCreation(User user, Currency currency) {
        KYCProfile kycProfile = kycProfileRepository.findByUser(user).orElseThrow(() -> new KycProfileNotFoundException("KYC profile not found."));

        // User must have approved KYC
        if (kycProfile.getStatus() != KYCStatus.VERIFIED){
            throw new IllegalArgumentException("KYC verification is required to create a wallet.");
        }

        switch (user.getCountry()){
            case ETHIOPIA -> validateEthiopianRules(kycProfile, currency);
            case UNITED_STATES -> validateUSARules(kycProfile, currency);
            default -> throw new IllegalArgumentException("Wallet creation is not allowed for users from this country.");
        }
    }

    private void validateEthiopianRules(KYCProfile kycProfile, Currency currency){
        switch (currency){
            case ETB -> {
            }

            case USD -> {
                if(kycProfile.getKycLevel() != KycLevel.ENHANCED){
                    throw new IllegalArgumentException("USD wallet requires Enhanced KYC.");
                }
            }

            default -> {
                throw new IllegalArgumentException(currency + " wallets are not available in Ethiopia.");
            }
        }
    }

    private void validateUSARules(KYCProfile kycProfile, Currency currency){
        switch (currency){
            case USD -> {
            }

            case ETB, EUR -> {
                if(kycProfile.getKycLevel() == KycLevel.NONE){
                    throw new IllegalArgumentException(currency + " wallet requires at least Basic KYC.");
                }
            }

            default -> {
                throw new IllegalArgumentException(currency + " wallets are not available in the USA.");
            }
        }
    }

    public void disableUserWallet(UUID walletId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));
        Wallet wallet = walletRepository.findByWalletIdAndUser(walletId, user).orElseThrow(() -> new WalletNotFoundException("Wallet not found."));
        wallet.setStatus(WalletStatus.DISABLED);
        walletRepository.save(wallet);
    }
}
