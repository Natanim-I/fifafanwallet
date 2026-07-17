package com.oasis.EtetePay.dto;

import com.oasis.EtetePay.enums.Country;

import java.util.UUID;

public record UserResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        Country country
) {}
