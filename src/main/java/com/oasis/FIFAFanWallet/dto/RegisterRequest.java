package com.oasis.FIFAFanWallet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Password is required.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must be eight or more characters containing at least one uppercase letter, one lowercase letter, one number, and one special character."
        )
        String password,
        @NotBlank(message = "First name is required.")
        @Size(max = 50, message = "First name shouldn't exceed 50 characters.")
        String firstName,
        @NotBlank(message = "Last name is required.")
        @Size(max = 50, message = "Last name shouldn't exceed 50 characters.")
        String lastName,
        @NotBlank(message = "Country is required.")
        @Pattern(
                regexp = "^[A-Z]{3}$",
                message = "Country code must be a valid 3-letter uppercase code."
        )
        String country
) {}