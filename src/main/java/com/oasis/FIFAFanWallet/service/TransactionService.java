package com.oasis.FIFAFanWallet.service;

import com.oasis.FIFAFanWallet.dto.ExchangeResponse;
import com.oasis.FIFAFanWallet.dto.TransactionRequest;
import com.oasis.FIFAFanWallet.dto.TransactionResponse;
import com.oasis.FIFAFanWallet.dto.TransferResponse;
import com.oasis.FIFAFanWallet.enums.*;
import com.oasis.FIFAFanWallet.exception.*;
import com.oasis.FIFAFanWallet.exception.IllegalArgumentException;
import com.oasis.FIFAFanWallet.model.Transaction;
import com.oasis.FIFAFanWallet.model.Wallet;
import com.oasis.FIFAFanWallet.model.auth.User;
import com.oasis.FIFAFanWallet.repo.TransactionRepository;
import com.oasis.FIFAFanWallet.repo.UserRepository;
import com.oasis.FIFAFanWallet.repo.WalletRepository;
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

    @Transactional
    public TransactionResponse deposit(UUID walletId, TransactionRequest transactionRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new WalletNotFoundException("Wallet not found!"));
        if(!wallet.getUser().getEmail().equals(email)){
            throw new AccessDeniedException("Wallet doesn't belong to this user.");
        }

        if(wallet.getStatus() == WalletStatus.DISABLED){
            throw new IllegalStateException("Wallet is Disabled.");
        }

        wallet.setBalance(wallet.getBalance().add(transactionRequest.amount()));

        Transaction transaction = new Transaction();
        transaction.setAmount(transactionRequest.amount());
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setWallet(wallet);
        transaction.setStatus(TransactionStatus.SUCCESS);

        Transaction savedTransaction = transactionRepository.save(transaction);

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
    public TransactionResponse withdraw(UUID walletId, TransactionRequest transactionRequest){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new WalletNotFoundException("Wallet not found!"));
        if(!wallet.getUser().getEmail().equals(email)){
            throw new AccessDeniedException("Wallet doesn't belong to this user.");
        }

        if(wallet.getStatus() == WalletStatus.DISABLED){
            throw new WalletIsDisabledException("Wallet is Disabled!");
        }

        if(wallet.getBalance().compareTo(transactionRequest.amount()) < 0){
            throw new InsufficientFundsException("Insufficient balance.");
        }

        wallet.setBalance(wallet.getBalance().subtract(transactionRequest.amount()));
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionRequest.amount());
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setWallet(wallet);
        transaction.setStatus(TransactionStatus.SUCCESS);

        Transaction savedTransaction = transactionRepository.save(transaction);

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

        if(senderId.equals(receiverId)){
            throw new IllegalArgumentException("Transfer to the same account is not supported.");
        }

        Wallet senderWallet = walletRepository.findById(senderId).orElseThrow(() -> new WalletNotFoundException("Wallet not found."));
        Wallet receiverWallet = walletRepository.findById(receiverId).orElseThrow(() -> new WalletNotFoundException("Wallet not found."));

        if(!senderWallet.getUser().getEmail().equals(email)){
            throw new AccessDeniedException("Wallet doesn't belong to this user.");
        }

        if(senderWallet.getStatus() == WalletStatus.DISABLED || receiverWallet.getStatus() == WalletStatus.DISABLED){
            throw new WalletIsDisabledException("Wallet is disabled.");
        }

        if(!senderWallet.getCurrency().equals(receiverWallet.getCurrency())){
            throw new CurrencyMismatchException("Cross-currency transfers are not supported.");
        }

        if(senderWallet.getBalance().compareTo(transactionRequest.amount()) < 0){
            throw new InsufficientFundsException("Insufficient balance.");
        }

        senderWallet.setBalance(senderWallet.getBalance().subtract(transactionRequest.amount()));
        receiverWallet.setBalance(receiverWallet.getBalance().add(transactionRequest.amount()));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

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

        if(fromWalletId.equals(toWalletId)){
            throw new IllegalArgumentException("Exchange to the same wallet is not supported.");
        }

        Wallet fromWallet = walletRepository.findById(fromWalletId).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
        Wallet toWallet = walletRepository.findById(toWalletId).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

        if(!fromWallet.getUser().getEmail().equals(email)){
            throw new AccessDeniedException("Wallet doesn't belong to this user.");
        }

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

        fromWallet.setBalance(fromWallet.getBalance().subtract(transactionRequest.amount()));
        BigDecimal rate = exchangeRateService.getCurrencyExchangeRate(fromWallet.getCurrency(), toWallet.getCurrency());
        BigDecimal convertedAmount = transactionRequest.amount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        toWallet.setBalance(toWallet.getBalance().add(convertedAmount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        Transaction exchangeOutTransaction = new Transaction();
        exchangeOutTransaction.setAmount(transactionRequest.amount());
        exchangeOutTransaction.setType(TransactionType.EXCHANGE_OUT);
        exchangeOutTransaction.setStatus(TransactionStatus.SUCCESS);
        exchangeOutTransaction.setWallet(fromWallet);

        Transaction exchangeInTransaction = new Transaction();
        exchangeInTransaction.setAmount(convertedAmount);
        exchangeInTransaction.setType(TransactionType.EXCHANGE_IN);
        exchangeInTransaction.setStatus(TransactionStatus.SUCCESS);
        exchangeInTransaction.setWallet(toWallet);

        transactionRepository.save(exchangeOutTransaction);
        transactionRepository.save(exchangeInTransaction);

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
            BigDecimal amount)
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));
        List<Transaction> transactions = transactionRepository.searchTransactions(
                user.getUserId(), type, currency, startDate, endDate, amount);

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
}
