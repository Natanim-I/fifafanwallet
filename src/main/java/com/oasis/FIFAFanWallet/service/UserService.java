package com.oasis.FIFAFanWallet.service;

import com.oasis.FIFAFanWallet.dto.UserResponse;
import com.oasis.FIFAFanWallet.exception.UserAlreadyExistsException;
import com.oasis.FIFAFanWallet.model.auth.User;
import com.oasis.FIFAFanWallet.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    //User registration with password being hashed
    public UserResponse registerUser(User user){
        //Checking if the email exists
        if(userRepository.existsByEmail(user.getEmail())){
            throw new UserAlreadyExistsException("User already exists with the specified Email");
        }
        //Setting hashed password
        user.setPasswordHash(encoder.encode(user.getPasswordHash()));

        //Saving user to database
        User registeredUser = userRepository.save(user);

        return new UserResponse(
                registeredUser.getUserId(), registeredUser.getEmail(), registeredUser.getFirstName(), registeredUser.getLastName(), registeredUser.getCountry());
    }
}
