package com.manager.cli_password_manager.core.exception.cache;

public class CacheInitializationException extends RuntimeException {
    public CacheInitializationException(String message) {
        super(message);
    }

    public CacheInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
