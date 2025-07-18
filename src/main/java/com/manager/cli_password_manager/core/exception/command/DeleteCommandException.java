package com.manager.cli_password_manager.core.exception.command;

public class DeleteCommandException extends RuntimeException {
    public DeleteCommandException(String message) {
        super(message);
    }

    public DeleteCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
