package com.oasis.FIFAFanWallet.controller;
import com.oasis.FIFAFanWallet.dto.RegisterRequest;
import com.oasis.FIFAFanWallet.dto.VerificationRequest;
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
@CrossOrigin()
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody RegisterRequest user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(user));
    }

    @GetMapping("verify")
    public ResponseEntity<String> verify(@RequestParam String verificationToken){
        return ResponseEntity.ok(userService.verifyAccount(verificationToken));
    }

    @PostMapping("resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestBody VerificationRequest verificationRequest){
        return ResponseEntity.ok(userService.resendVerificationEmail(verificationRequest));
    }

    @GetMapping("/details")
    public ResponseEntity<UserResponse> getUserDetails(){
        return ResponseEntity.ok(userService.getUserDetails());
    }
}
