package com.manager.cli_password_manager.core.exception.file.loader;

public class FileLoaderException extends RuntimeException {
    public FileLoaderException(String message) {
        super(message);
    }

    public FileLoaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
