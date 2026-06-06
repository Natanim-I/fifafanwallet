package com.oasis.FIFAFanWallet.model;

import java.util.UUID;

public record UserResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String country
) {}
