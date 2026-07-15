package com.oasis.EtetePay.service;

import com.oasis.EtetePay.dto.*;
import com.oasis.EtetePay.exception.InvalidVerificationToken;
import com.oasis.EtetePay.exception.UserAlreadyExistsException;
import com.oasis.EtetePay.exception.UserNotFoundException;
import com.oasis.EtetePay.model.auth.PasswordResetToken;
import com.oasis.EtetePay.model.auth.User;
import com.oasis.EtetePay.repo.PasswordResetTokenRepo;
import com.oasis.EtetePay.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    //User registration with password being hashed
    public UserResponse registerUser(RegisterRequest user){
        //Checking if the email exists
        if(userRepository.existsByEmail(user.email())){
            throw new UserAlreadyExistsException("User already exists with the specified Email");
        }

        User newUser = new User();
        newUser.setEmail(user.email());
        newUser.setPasswordHash(passwordEncoder.encode(user.password()));
        newUser.setFirstName(user.firstName());
        newUser.setLastName(user.lastName());
        newUser.setCountry(user.country());
        newUser.setEnabled(false);
        String verificationToken = UUID.randomUUID().toString();
        newUser.setVerificationToken(verificationToken);
        newUser.setTokenExpiry(LocalDateTime.now().plusHours(1));

        //Saving user to database
        User registeredUser = userRepository.save(newUser);

        emailService.sendVerificationEmail(newUser.getEmail(), verificationToken);

        return new UserResponse(
                registeredUser.getUserId(), registeredUser.getEmail(), registeredUser.getFirstName(), registeredUser.getLastName(), registeredUser.getCountry());
    }

    public UserResponse getUserDetails() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));
        return new UserResponse(user.getUserId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getCountry());
    }

    public AccountVerificationResponse verifyAccount(String verificationToken) {
        User user = userRepository.findByVerificationToken(verificationToken).orElseThrow(() -> new InvalidVerificationToken("Invalid token."));

        if (user.isEnabled()) {
            return new AccountVerificationResponse("Account already verified.");
        }

        if(user.getTokenExpiry().isBefore(LocalDateTime.now())){
            throw new InvalidVerificationToken("Expired verification token. Please request a new verification email.");
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return new AccountVerificationResponse("Account verified successfully.");
    }

    public String resendVerificationEmail(VerificationRequest verificationRequest) {
        User user = userRepository.findByEmailAndVerificationToken(verificationRequest.email(), verificationRequest.verificationToken()).orElseThrow(() -> new UserNotFoundException("User not found."));

        if (user.isEnabled()) {
            return "Account already verified. You can sign in.";
        }

        if(user.getTokenExpiry().isBefore(LocalDateTime.now())){
            user.setTokenExpiry(LocalDateTime.now().plusHours(1));
        }
        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());

        return "Verification email is sent to the associated account email.";
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found!"));
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String resetToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        PasswordResetToken passResetToken = new PasswordResetToken();
        passResetToken.setUser(user);
        passResetToken.setToken(resetToken);
        passResetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        passwordResetTokenRepo.save(passResetToken);

        emailService.sendForgotPassEmail(user.getEmail(), resetToken);
        return "Password reset email sent.";
    }


    public String resetPassword(String newPassword, String resetToken) {
        PasswordResetToken passResetToken = passwordResetTokenRepo.findByToken(resetToken).orElseThrow(() -> new InvalidVerificationToken("Invalid reset token."));
        if(passResetToken.getExpiryDate().isBefore(LocalDateTime.now())){
            passwordResetTokenRepo.delete(passResetToken);
            throw new InvalidVerificationToken("Expired reset token. Please request a new password reset email.");
        }
        User user = passResetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepo.delete(passResetToken);
        emailService.sendPassResetConfirmationEmail(user.getEmail());
        return "Password reset successfully.";
    }
}
