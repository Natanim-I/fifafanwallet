package com.oasis.FIFAFanWallet.service;

import com.oasis.FIFAFanWallet.exception.InvalidRefreshTokenException;
import com.oasis.FIFAFanWallet.model.auth.RefreshToken;
import com.oasis.FIFAFanWallet.repo.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(String email){
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setEmail(email);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpirationDate(Instant.now().plusMillis(refreshExpiration));

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyToken(String token){
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(
                () -> new InvalidRefreshTokenException("Invalid or Expired Refresh Token."));

        if(refreshToken.getExpirationDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRefreshTokenException("Invalid or Expired Refresh Token.");
        }
        return refreshToken;
    }
}
