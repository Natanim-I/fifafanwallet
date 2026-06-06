package com.oasis.FIFAFanWallet.controller;
import com.oasis.FIFAFanWallet.model.User;
import com.oasis.FIFAFanWallet.model.UserResponse;
import com.oasis.FIFAFanWallet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("register")
    public ResponseEntity<UserResponse> registerUser(User user){
        User registeredUser = userService.registerUser(user);
        UserResponse userResponse = new UserResponse(
                registeredUser.getUserId(), registeredUser.getEmail(), registeredUser.getFirstName(), registeredUser.getLastName(), registeredUser.getCountry());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
}
