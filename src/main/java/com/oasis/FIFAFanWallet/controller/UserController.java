package com.oasis.FIFAFanWallet.controller;
import com.oasis.FIFAFanWallet.dto.RegisterRequest;
import com.oasis.FIFAFanWallet.model.auth.User;
import com.oasis.FIFAFanWallet.dto.UserResponse;
import com.oasis.FIFAFanWallet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody RegisterRequest user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(user));
    }

    @GetMapping("/details")
    public ResponseEntity<UserResponse> getUserDetails(){
        return ResponseEntity.ok(userService.getUserDetails());
    }
}
