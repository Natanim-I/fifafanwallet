package com.oasis.EtetePay.repo;

import com.oasis.EtetePay.enums.Currency;
import com.oasis.EtetePay.enums.WalletStatus;
import com.oasis.EtetePay.model.Wallet;
import com.oasis.EtetePay.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    List<Wallet> findAllByUserAndStatus(User user, WalletStatus status);

    boolean existsByUserAndCurrency(User user, Currency currency);

    Optional<Wallet> findByWalletIdAndUser(UUID walletId, User user);
}