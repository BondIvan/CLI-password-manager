package com.manager.cli_password_manager.core.entity.dto.export;

import lombok.Getter;

@Getter
public abstract class AbstractExportParameters {
    private final String version;
    private final String algorithm;
    private final String encryptedResult;

    public AbstractExportParameters(String algorithm, String exportData) {
        this.version = "0.0.1";
        this.algorithm = algorithm;
        this.encryptedResult = exportData;
    }
}
