package com.raj.wallet.common.exception;

import com.raj.wallet.wallet.exception.InsufficientBalanceException;
import com.raj.wallet.wallet.exception.WalletAlreadyExistsException;
import com.raj.wallet.wallet.exception.WalletFrozenException;
import com.raj.wallet.wallet.exception.WalletNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleWalletNotFound(
            WalletNotFoundException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.NOT_FOUND.value(),
                        "Wallet Not Found",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(WalletFrozenException.class)
    public ResponseEntity<ApiErrorResponse> handleFrozen(
            WalletFrozenException ex,
            HttpServletRequest request) {

        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Wallet Frozen",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiErrorResponse> handleBalance(
            InsufficientBalanceException ex,
            HttpServletRequest request) {

        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Insufficient Balance",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation Error",
                        message,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(WalletAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse>
    handleWalletExists(WalletAlreadyExistsException ex, HttpServletRequest request){

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(Instant.now(), 409, "Wallet Already Exists", ex.getMessage(), request.getRequestURI()));

    }

}