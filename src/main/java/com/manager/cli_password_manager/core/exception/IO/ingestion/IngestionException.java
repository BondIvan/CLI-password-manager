package com.manager.cli_password_manager.core.exception.IO.ingestion;

public class IngestionException extends RuntimeException {
    public IngestionException(String message) {
        super(message);
    }

    public IngestionException(Throwable cause) {
        super(cause);
    }

    public IngestionException(String message, Throwable cause) {
        super(message, cause);
    }
}
