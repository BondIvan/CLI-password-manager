package com.manager.cli_password_manager.core.exception.Initialization;

import org.springframework.boot.ExitCodeGenerator;

public class InitializerException extends RuntimeException implements ExitCodeGenerator {
    public InitializerException(String message) {
        super(message);
    }

    public InitializerException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getExitCode() {
        return 5;
    }
}
