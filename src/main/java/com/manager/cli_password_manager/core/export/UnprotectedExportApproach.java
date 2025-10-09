package com.manager.cli_password_manager.core.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.manager.cli_password_manager.core.service.file.creator.SecureFileCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class UnprotectedExportApproach implements ExportProtectApproach {
    private final ObjectMapper objectMapper;
    private final SecureFileCreator fileCreator;

    public UnprotectedExportApproach(ObjectMapper objectMapper,
                                     SecureFileCreator fileCreator) {
        this.objectMapper = objectMapper;
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.fileCreator = fileCreator;
    }

    @Override
    public void export(Path exportPath, DataProducer producer, char[] password) throws IOException {

    }
}
