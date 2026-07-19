package com.oasis.EtetePay.service;

import com.oasis.EtetePay.dto.*;
import com.oasis.EtetePay.enums.*;
import com.oasis.EtetePay.exception.*;
import com.oasis.EtetePay.exception.IllegalArgumentException;
import com.oasis.EtetePay.helpers.Helper;
import com.oasis.EtetePay.model.Transaction;
import com.oasis.EtetePay.model.Wallet;
import com.oasis.EtetePay.model.auth.User;
import com.oasis.EtetePay.repo.TransactionRepository;
import com.oasis.EtetePay.repo.UserRepository;
import com.oasis.EtetePay.repo.WalletRepository;
import com.oasis.EtetePay.specification.TransactionSpecification;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;
    private final UserRepository userRepository;
    private final Helper helper;

    @Transactional
    public DepositInitiationResponse initiateDeposit(UUID walletId, TransactionRequest transactionRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));
        Wallet wallet = walletRepository.findByWalletIdAndUser(walletId, user).orElseThrow(() -> new WalletNotFoundException("Wallet associated with this user not found!"));


        if(wallet.getStatus() == WalletStatus.DISABLED){
            throw new IllegalStateException("Wallet is Disabled.");
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(transactionRequest.amount());
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setWallet(wallet);
        transaction.setStatus(TransactionStatus.PENDING);

        transactionRepository.save(transaction);

        long stripeAmount = helper.toStripeAmount(transactionRequest.amount());

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(stripeAmount)
                .setCurrency(wallet.getCurrency().name().toLowerCase())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .putMetadata("transactionId", transaction.getTransactionId().toString())
                .build();

        PaymentIntent intent;
        try {
            intent = PaymentIntent.create(params);
        } catch (StripeException e) {
            throw new StripePaymentException("Failed to create payment intent: " + e.getMessage());
        }

        transaction.setPaymentIntentId(intent.getId());
        transactionRepository.save(transaction);

        return new DepositInitiationResponse(transaction.getTransactionId(), intent.getClientSecret());
    }

    @Transactional
    public TransactionResponse withdraw(UUID walletId, TransactionRequest transactionRequest){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));
        Wallet wallet = walletRepository.findByWalletIdAndUser(walletId, user).orElseThrow(() -> new WalletNotFoundException("Wallet associated with this user not found!"));

        if(wallet.getStatus() == WalletStatus.DISABLED){
            throw new WalletIsDisabledException("Wallet is Disabled!");
        }

        if(wallet.getBalance().compareTo(transactionRequest.amount()) < 0){
            throw new InsufficientFundsException("Insufficient balance.");
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(transactionRequest.amount());
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setWallet(wallet);
        transaction.setStatus(TransactionStatus.SUCCESS);

        Transaction savedTransaction = transactionRepository.save(transaction);

        wallet.setBalance(wallet.getBalance().subtract(transactionRequest.amount()));

        return new TransactionResponse(
                savedTransaction.getTransactionId(),
                savedTransaction.getWallet().getWalletId(),
                savedTransaction.getAmount(),
                savedTransaction.getType(),
                savedTransaction.getStatus(),
                savedTransaction.getCreatedAt()
        );
    }

    @Transactional
    public TransferResponse transfer(UUID senderId, UUID receiverId, TransactionRequest transactionRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));

        if(senderId.equals(receiverId)){
            throw new IllegalArgumentException("Transfer to the same account is not supported.");
        }

        Wallet senderWallet = walletRepository.findByWalletIdAndUser(senderId, user).orElseThrow(() -> new WalletNotFoundException("Wallet associated with this user not found."));
        Wallet receiverWallet = walletRepository.findById(receiverId).orElseThrow(() -> new WalletNotFoundException("Destination wallet not found."));

        if(senderWallet.getStatus() == WalletStatus.DISABLED || receiverWallet.getStatus() == WalletStatus.DISABLED){
            throw new WalletIsDisabledException("Wallet is disabled.");
        }

        if(!senderWallet.getCurrency().equals(receiverWallet.getCurrency())){
            throw new CurrencyMismatchException("Cross-currency transfers are not supported.");
        }

        if(senderWallet.getBalance().compareTo(transactionRequest.amount()) < 0){
            throw new InsufficientFundsException("Insufficient balance.");
        }

        Transaction sentTransaction = new Transaction();
        sentTransaction.setAmount(transactionRequest.amount());
        sentTransaction.setType(TransactionType.TRANSFER_OUT);
        sentTransaction.setWallet(senderWallet);
        sentTransaction.setStatus(TransactionStatus.SUCCESS);

        Transaction receivedTransaction = new Transaction();
        receivedTransaction.setAmount(transactionRequest.amount());
        receivedTransaction.setType(TransactionType.TRANSFER_IN);
        receivedTransaction.setWallet(receiverWallet);
        receivedTransaction.setStatus(TransactionStatus.SUCCESS);

        transactionRepository.save(sentTransaction);
        transactionRepository.save(receivedTransaction);

        senderWallet.setBalance(senderWallet.getBalance().subtract(transactionRequest.amount()));
        receiverWallet.setBalance(receiverWallet.getBalance().add(transactionRequest.amount()));

        return new TransferResponse(
                sentTransaction.getTransactionId(),
                senderWallet.getWalletId(),
                receiverWallet.getWalletId(),
                transactionRequest.amount(),
                sentTransaction.getType(),
                sentTransaction.getStatus(),
                sentTransaction.getCreatedAt()
        );
    }

    @Transactional
    public ExchangeResponse exchange(UUID fromWalletId, UUID toWalletId, TransactionRequest transactionRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));

        if(fromWalletId.equals(toWalletId)){
            throw new IllegalArgumentException("Exchange to the same wallet is not supported.");
        }

        Wallet fromWallet = walletRepository.findByWalletIdAndUser(fromWalletId, user).orElseThrow(() -> new WalletNotFoundException("Wallet associated to this user not found"));
        Wallet toWallet = walletRepository.findByWalletIdAndUser(toWalletId, user).orElseThrow(() -> new WalletNotFoundException("Wallet associated to this not found"));

        if(!fromWallet.getUser().getUserId().equals(toWallet.getUser().getUserId())){
            throw new IllegalArgumentException("Exchange to different user wallet is not supported.");
        }

        if(fromWallet.getStatus() == WalletStatus.DISABLED || toWallet.getStatus() == WalletStatus.DISABLED){
            throw new WalletIsDisabledException("Wallet is Disabled.");
        }

        if(fromWallet.getCurrency().equals(toWallet.getCurrency())){
            throw new java.lang.IllegalArgumentException("Exchange to the same currency wallet is not supported.");
        }

        if(fromWallet.getBalance().compareTo(transactionRequest.amount()) < 0){
            throw new InsufficientFundsException("Insufficient funds.");
        }

        BigDecimal rate = exchangeRateService.getCurrencyExchangeRate(fromWallet.getCurrency(), toWallet.getCurrency());
        BigDecimal convertedAmount = transactionRequest.amount().multiply(rate).setScale(2, RoundingMode.HALF_UP);

        Transaction exchangeOutTransaction = new Transaction();
        exchangeOutTransaction.setAmount(transactionRequest.amount());
        exchangeOutTransaction.setType(TransactionType.EXCHANGE_OUT);
        exchangeOutTransaction.setWallet(fromWallet);
        exchangeOutTransaction.setStatus(TransactionStatus.SUCCESS);

        Transaction exchangeInTransaction = new Transaction();
        exchangeInTransaction.setAmount(convertedAmount);
        exchangeInTransaction.setType(TransactionType.EXCHANGE_IN);
        exchangeInTransaction.setWallet(toWallet);
        exchangeInTransaction.setStatus(TransactionStatus.SUCCESS);

        transactionRepository.save(exchangeOutTransaction);
        transactionRepository.save(exchangeInTransaction);

        fromWallet.setBalance(fromWallet.getBalance().subtract(transactionRequest.amount()));
        toWallet.setBalance(toWallet.getBalance().add(convertedAmount));

        return new ExchangeResponse(
                exchangeOutTransaction.getTransactionId(),
                fromWallet.getWalletId(),
                toWallet.getWalletId(),
                fromWallet.getCurrency(),
                toWallet.getCurrency(),
                transactionRequest.amount(),
                exchangeOutTransaction.getType(),
                exchangeOutTransaction.getStatus(),
                exchangeOutTransaction.getCreatedAt()
        );
    }

    public List<TransactionResponse> getAllTransactionUser(
            TransactionType type,
            Currency currency,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            BigDecimal amount
    )
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));
        List<Transaction> transactions = transactionRepository.findAll(
                TransactionSpecification.filter(
                        user.getUserId(), type, currency, startDate, endDate, minAmount, maxAmount, amount)
        );

        return transactions.stream()
                .map(transaction -> new TransactionResponse(
                        transaction.getTransactionId(),
                        transaction.getWallet().getWalletId(),
                        transaction.getAmount(),
                        transaction.getType(),
                        transaction.getStatus(),
                        transaction.getCreatedAt()))
                .toList();
    }

    public TransactionResponse getTransactionById(UUID transactionId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));
        Transaction transaction = transactionRepository.findByTransactionIdAndWallet_User(transactionId, user)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found for this user."));

        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getWallet().getWalletId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getCreatedAt());
    }
}
