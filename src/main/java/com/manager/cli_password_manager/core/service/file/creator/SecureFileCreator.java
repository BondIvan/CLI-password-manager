package com.manager.cli_password_manager.core.service.file.creator;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class SecureFileCreator extends AbstractFileCreator {
    @Override
    protected void create(Path path) throws IOException {
        Files.createFile(path);
    }

    @Override
    protected Path createTmp(Path path, String prefix) throws IOException {
        return Files.createTempFile(path, prefix, null);
    }
}
