package com.manager.cli_password_manager.security.encrypt;

import com.manager.cli_password_manager.core.entity.dto.export.EncryptedExportContainer;
import com.manager.cli_password_manager.core.entity.enums.IOEncryptorAlgorithm;

public interface IOEncryptor {
    EncryptedExportContainer encrypt(byte[] plainTextBytes, char[] password);
    byte[] decrypt(String plainText, String salt, String iv, char[] password);
    IOEncryptorAlgorithm getExportAlgorithm();
}
