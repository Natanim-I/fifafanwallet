package com.oasis.FIFAFanWallet.repo;

import com.oasis.FIFAFanWallet.model.auth.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String username);

    boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);
    Optional<User> findByEmailAndVerificationToken(String email, String verificationToken);
}
