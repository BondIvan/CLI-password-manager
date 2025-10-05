package com.manager.cli_password_manager.core.entity.dto.export;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AESExportParameters extends AbstractExportParameters {
    private String salt;
    private String iv;
    private String kdf;
    private int iterations;

    public AESExportParameters(String algorithm, String exportData) {
        super(algorithm, exportData);
    }
}
