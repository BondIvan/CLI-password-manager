package com.manager.cli_password_manager.core.exception.command;

public class CheckCommandException extends RuntimeException {
    public CheckCommandException(String message) {
        super(message);
    }

    public CheckCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
