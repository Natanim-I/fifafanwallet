package com.oasis.FIFAFanWallet.service;

import com.oasis.FIFAFanWallet.dto.RegisterRequest;
import com.oasis.FIFAFanWallet.dto.UserResponse;
import com.oasis.FIFAFanWallet.exception.UserAlreadyExistsException;
import com.oasis.FIFAFanWallet.exception.UserNotFoundException;
import com.oasis.FIFAFanWallet.model.auth.User;
import com.oasis.FIFAFanWallet.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

        //Saving user to database
        User registeredUser = userRepository.save(newUser);

        return new UserResponse(
                registeredUser.getUserId(), registeredUser.getEmail(), registeredUser.getFirstName(), registeredUser.getLastName(), registeredUser.getCountry());
    }

    public UserResponse getUserDetails() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));
        return new UserResponse(user.getUserId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getCountry());
    }
}
