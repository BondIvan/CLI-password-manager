package com.manager.cli_password_manager.core.exception.command;

public class AddCommandException extends RuntimeException {
    public AddCommandException(String message) {
        super(message);
    }

    public AddCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
