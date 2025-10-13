package com.manager.cli_password_manager.security.encrypt;

import com.manager.cli_password_manager.core.entity.dto.export.EncryptedExportContainer;
import com.manager.cli_password_manager.core.entity.enums.ExportEncryptorAlgorithm;

public interface ExportEncryptor {
    EncryptedExportContainer encrypt(byte[] plainTextBytes, char[] password);
    ExportEncryptorAlgorithm getExportAlgorithm();
}
