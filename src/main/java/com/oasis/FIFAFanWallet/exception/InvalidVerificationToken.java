package com.oasis.FIFAFanWallet.exception;

public class InvalidVerificationToken extends RuntimeException {
    public InvalidVerificationToken(String message) {
        super(message);
    }
}
