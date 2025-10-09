package com.manager.cli_password_manager.core.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.manager.cli_password_manager.core.service.file.creator.SecureFileCreator;
import com.manager.cli_password_manager.security.encrypt.Encrypting;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class ProtectedExportApproach implements ExportProtectApproach {
    private final ObjectMapper objectMapper;
    private final SecureFileCreator fileCreator;
    private final Encrypting encrypting;

    public ProtectedExportApproach(ObjectMapper objectMapper,
                                   SecureFileCreator fileCreator,
                                   Encrypting encrypting) {
        this.objectMapper = objectMapper;
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.fileCreator = fileCreator;
        this.encrypting = encrypting;
    }

    @Override
    public void export(Path exportPath, DataProducer producer, char[] password) {

    }
}
