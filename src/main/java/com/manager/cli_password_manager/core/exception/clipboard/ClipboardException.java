package com.manager.cli_password_manager.core.exception.clipboard;

public class
ClipboardException extends RuntimeException {
    public ClipboardException(String message) {
        super(message);
    }

    public ClipboardException(String message, Throwable cause) {
        super(message, cause);
    }
}
