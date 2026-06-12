package com.oasis.FIFAFanWallet.dto;

import java.util.UUID;

public record RegisterRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String country
) {}