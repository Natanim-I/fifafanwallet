package com.oasis.EtetePay.controller;
import com.oasis.EtetePay.dto.*;
import com.oasis.EtetePay.service.KycService;
import com.oasis.EtetePay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin()
public class UserController {

    private final UserService userService;
    private final KycService kycService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody RegisterRequest user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(user));
    }

    @GetMapping("verify")
    public ResponseEntity<AccountVerificationResponse> verify(@RequestParam String verificationToken){
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

    @PostMapping("forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPassRequest forgotPassRequest){
        return ResponseEntity.ok(userService.forgotPassword(forgotPassRequest.email()));
    }

    @PostMapping("reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPassRequest resetPassRequest){
        return ResponseEntity.ok(userService.resetPassword(resetPassRequest.newPassword(), resetPassRequest.token()));
    }

    @PostMapping(value = "submit-kyc", consumes = {"multipart/form-data"})
    public ResponseEntity<KycResponse> processKyc(@RequestPart KycRequest kycRequest, @RequestPart MultipartFile idFrontImage, @RequestPart MultipartFile idBackImage, @RequestPart MultipartFile selfieImage){
        return ResponseEntity.ok(kycService.processKyc(kycRequest, idFrontImage, idBackImage, selfieImage));
    }
}
