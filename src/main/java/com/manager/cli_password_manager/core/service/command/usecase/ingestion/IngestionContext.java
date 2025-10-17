package com.manager.cli_password_manager.core.service.command.usecase.ingestion;

import java.nio.file.Path;

public record IngestionContext(
        Path filePath,
        String password
) { }
