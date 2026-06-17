package com.oasis.FIFAFanWallet.repo;

import com.oasis.FIFAFanWallet.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
