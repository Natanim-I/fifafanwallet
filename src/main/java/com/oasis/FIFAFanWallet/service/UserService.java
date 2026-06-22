package com.oasis.FIFAFanWallet.service;

import com.oasis.FIFAFanWallet.dto.RegisterRequest;
import com.oasis.FIFAFanWallet.dto.UserResponse;
import com.oasis.FIFAFanWallet.dto.VerificationRequest;
import com.oasis.FIFAFanWallet.exception.IllegalArgumentException;
import com.oasis.FIFAFanWallet.exception.InvalidVerificationToken;
import com.oasis.FIFAFanWallet.exception.UserAlreadyExistsException;
import com.oasis.FIFAFanWallet.exception.UserNotFoundException;
import com.oasis.FIFAFanWallet.model.auth.User;
import com.oasis.FIFAFanWallet.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

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

    public String verifyAccount(String verificationToken) {
        User user = userRepository.findByVerificationToken(verificationToken).orElseThrow(() -> new InvalidVerificationToken("Invalid token."));

        if(user.getTokenExpiry().isBefore(LocalDateTime.now())){
            throw new InvalidVerificationToken("Expired verification token. Please request a new verification email.");
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return "Account verified successfully.";
    }

    public String resendVerificationEmail(VerificationRequest verificationRequest) {
        User user = userRepository.findByEmailAndVerificationToken(verificationRequest.email(), verificationRequest.verificationToken()).orElseThrow(() -> new UserNotFoundException("User not found."));

        if(user.getTokenExpiry().isBefore(LocalDateTime.now())){
            user.setTokenExpiry(LocalDateTime.now().plusHours(1));
        }
        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());

        return "Verification email is sent to the associated account email.";
    }
}
