package com.oasis.FIFAFanWallet.exception;

import com.oasis.FIFAFanWallet.dto.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GeneralExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> usernameNotFoundHandler(UsernameNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, "Invalid username or password."));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> badCredintialsHandler(BadCredentialsException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(401, "Invalid username or password."));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> userExistsExceptionHandler(UserAlreadyExistsException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, ex.getMessage()));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> invalidRefreshTokenHandler(InvalidRefreshTokenException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(401, ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> userNotFoundHandler(UserNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(WalletAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> walletAlreadyExistsHandler(WalletAlreadyExistsException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, ex.getMessage()));
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> walletNotFoundHandler(WalletNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDeniedHandler(AccessDeniedException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(403, ex.getMessage()));
    }

    @ExceptionHandler(WalletIsDisabledException.class)
    public ResponseEntity<ErrorResponse> walletIsDisabledHandler(WalletIsDisabledException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, ex.getMessage()));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> insufficientFundsHandler(InsufficientFundsException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, ex.getMessage()));
    }

    @ExceptionHandler(CurrencyMismatchException.class)
    public ResponseEntity<ErrorResponse> currencyMismatchHandler(CurrencyMismatchException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(409, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> illegalArgumentHandler(IllegalArgumentException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400, ex.getMessage()));
    }

    @ExceptionHandler(CurrencyExchangeException.class)
    public ResponseEntity<ErrorResponse> currencyExchangeExceptionHandler(CurrencyExchangeException ex){
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorResponse(503, ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, "Database constraints violation."));
    }

    @ExceptionHandler(BudgetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBudgetNotFound(BudgetNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, ex.getMessage()));
    }
}
