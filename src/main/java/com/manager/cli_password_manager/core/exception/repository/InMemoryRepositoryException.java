package com.manager.cli_password_manager.core.exception.repository;

public class InMemoryRepositoryException extends RuntimeException {
    public InMemoryRepositoryException(String message) {
        super(message);
    }

    public InMemoryRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
