package com.oasis.EtetePay.exception;

public class FailedToUploadToS3Exception extends RuntimeException {
    public FailedToUploadToS3Exception(String message) {
        super(message);
    }
}
