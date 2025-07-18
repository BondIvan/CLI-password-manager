package com.manager.cli_password_manager.core.exception.vault;

public class VaultException extends RuntimeException {
    public VaultException(String message) {
        super(message);
    }

    public VaultException(String message, Throwable cause) {
        super(message, cause);
    }
}
