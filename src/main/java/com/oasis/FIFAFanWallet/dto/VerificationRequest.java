package com.oasis.FIFAFanWallet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerificationRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email format.")
        String email,
        @NotBlank(message = "Verification token is required.")
        String verificationToken
) {}
