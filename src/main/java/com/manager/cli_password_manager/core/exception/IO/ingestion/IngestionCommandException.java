package com.manager.cli_password_manager.core.exception.IO.ingestion;

public class IngestionCommandException extends RuntimeException {
    public IngestionCommandException(Throwable cause) {
        super(cause);
    }

    public IngestionCommandException(String message) {
        super(message);
    }

    public IngestionCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
