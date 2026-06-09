package com.oasis.FIFAFanWallet.service;

import com.oasis.FIFAFanWallet.exception.InvalidRefreshTokenException;
import com.oasis.FIFAFanWallet.model.auth.RefreshToken;
import com.oasis.FIFAFanWallet.repo.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(String email){
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setEmail(email);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpirationDate(Instant.now().plusMillis(refreshExpiration));

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyToken(String token){
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(
                () -> new InvalidRefreshTokenException("Invalid or expired refresh token."));

        if(refreshToken.getExpirationDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRefreshTokenException("Invalid or expired refresh token.");
        }
        return refreshToken;
    }
}
