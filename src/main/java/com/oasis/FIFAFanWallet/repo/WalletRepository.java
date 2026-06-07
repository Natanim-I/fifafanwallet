package com.oasis.FIFAFanWallet.repo;

import com.oasis.FIFAFanWallet.enums.Currency;
import com.oasis.FIFAFanWallet.model.Wallet;
import com.oasis.FIFAFanWallet.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    List<Wallet> findAllByUser(User user);

    boolean existsByUserAndCurrency(User user, Currency currency);

    Optional<Wallet> findByWalletIdAndUser_UserId(UUID walletId, UUID userId);
}