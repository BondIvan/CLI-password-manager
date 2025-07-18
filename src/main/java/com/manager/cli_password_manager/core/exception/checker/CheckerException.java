package com.manager.cli_password_manager.core.exception.checker;

public class CheckerException extends RuntimeException {
    public CheckerException(String message) {
        super(message);
    }

    public CheckerException(Throwable cause) {
        super(cause);
    }

    public CheckerException(String message, Throwable cause) {
        super(message, cause);
    }
}
