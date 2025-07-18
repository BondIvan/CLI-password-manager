package com.manager.cli_password_manager.core.exception.file.loader;

import java.io.IOException;

public class FileCreatorException extends IOException {
    public FileCreatorException(String message) {
        super(message);
    }

    public FileCreatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
