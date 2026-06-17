package com.oasis.FIFAFanWallet.service;

import com.oasis.FIFAFanWallet.dto.PaymentRequest;
import com.oasis.FIFAFanWallet.dto.PaymentResponse;
import com.oasis.FIFAFanWallet.enums.Currency;
import com.oasis.FIFAFanWallet.enums.PaymentStatus;
import com.oasis.FIFAFanWallet.enums.TransactionStatus;
import com.oasis.FIFAFanWallet.enums.TransactionType;
import com.oasis.FIFAFanWallet.exception.BudgetNotFoundException;
import com.oasis.FIFAFanWallet.exception.InsufficientFundsException;
import com.oasis.FIFAFanWallet.exception.UserNotFoundException;
import com.oasis.FIFAFanWallet.exception.WalletNotFoundException;
import com.oasis.FIFAFanWallet.model.Budget;
import com.oasis.FIFAFanWallet.model.Payment;
import com.oasis.FIFAFanWallet.model.Transaction;
import com.oasis.FIFAFanWallet.model.Wallet;
import com.oasis.FIFAFanWallet.model.auth.User;
import com.oasis.FIFAFanWallet.repo.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final ExchangeRateService exchangeRateService;

    @Transactional
    public PaymentResponse makePayment(PaymentRequest paymentRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));

        Wallet wallet = walletRepository.findByWalletIdAndUser(paymentRequest.walletId(), user).orElseThrow(() -> new WalletNotFoundException("Wallet associated with this user not found."));

        if(wallet.getBalance().compareTo(paymentRequest.amount()) < 0){
            throw new InsufficientFundsException("Insufficient funds.");
        }

        wallet.setBalance(wallet.getBalance().subtract(paymentRequest.amount()));

        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(paymentRequest.amount());
        transaction.setType(TransactionType.PAYMENT);
        transaction.setCategory(paymentRequest.budgetCategory());
        transaction.setStatus(TransactionStatus.SUCCESS);

        transactionRepository.save(transaction);

        Budget budget = budgetRepository.findBudgets(
                user.getUserId(),
                paymentRequest.budgetCategory(),
                LocalDateTime.now()
        ).orElseThrow(() -> new BudgetNotFoundException("Budget not found."));

        if(budget.getCurrency() == wallet.getCurrency()){
            budget.setSpentAmount(budget.getSpentAmount().add(paymentRequest.amount()).setScale(2, RoundingMode.HALF_UP));
        } else {
            BigDecimal rate = exchangeRateService.getCurrencyExchangeRate(wallet.getCurrency(), budget.getCurrency());
            BigDecimal paymentAmount = paymentRequest.amount().multiply(rate);
            budget.setSpentAmount(budget.getSpentAmount().add(paymentAmount).setScale(2, RoundingMode.HALF_UP));
        }

        Payment savedPayment = new Payment();
        savedPayment.setTransaction(transaction);
        savedPayment.setUserId(user.getUserId());
        savedPayment.setWalletId(wallet.getWalletId());
        savedPayment.setAmount(paymentRequest.amount());
        savedPayment.setBudgetCategory(paymentRequest.budgetCategory());
        savedPayment.setMerchantName(paymentRequest.merchantName());
        savedPayment.setDescription(paymentRequest.description());
        savedPayment.setStatus(PaymentStatus.COMPLETED);


        return new PaymentResponse(
                savedPayment.getPaymentId(),
                user.getUserId(),
                wallet.getWalletId(),
                transaction.getTransactionId(),
                savedPayment.getAmount(),
                savedPayment.getBudgetCategory(),
                savedPayment.getMerchantName(),
                savedPayment.getDescription(),
                savedPayment.getStatus(),
                savedPayment.getCreatedAt()
        );
    }
}
