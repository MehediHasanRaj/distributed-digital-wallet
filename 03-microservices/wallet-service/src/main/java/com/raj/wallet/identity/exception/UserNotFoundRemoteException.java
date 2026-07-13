package com.raj.wallet.identity.exception;

public class UserNotFoundRemoteException extends RuntimeException {
    public UserNotFoundRemoteException(String userId) {
        super("User not found in Identity Service: " + userId);
    }

}