package com.oasis.FIFAFanWallet.controller;

import com.oasis.FIFAFanWallet.dto.AuthResponse;
import com.oasis.FIFAFanWallet.dto.LoginRequest;

import com.oasis.FIFAFanWallet.dto.RefreshTokenRequest;
import com.oasis.FIFAFanWallet.model.auth.RefreshToken;
import com.oasis.FIFAFanWallet.service.JwtService;
import com.oasis.FIFAFanWallet.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        String token = jwtService.generateToken(loginRequest.email());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(loginRequest.email());
        return ResponseEntity.ok(new AuthResponse(token, refreshToken.getToken()));
    }

    @PostMapping("refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest){
        RefreshToken refreshToken = refreshTokenService.verifyToken(refreshTokenRequest.refreshToken());
        String token = jwtService.generateToken(refreshToken.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, refreshToken.getToken()));
    }
}
