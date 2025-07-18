package com.manager.cli_password_manager.core.exception.command;

public class ReplaceCommandException extends RuntimeException {
    public ReplaceCommandException(String message) {
        super(message);
    }

    public ReplaceCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
