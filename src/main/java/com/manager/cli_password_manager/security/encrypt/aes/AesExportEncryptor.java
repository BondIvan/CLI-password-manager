package com.manager.cli_password_manager.security.encrypt.aes;

import com.manager.cli_password_manager.core.entity.dto.export.EncryptedExportContainer;
import com.manager.cli_password_manager.core.entity.enums.ExportEncryptorAlgorithm;
import com.manager.cli_password_manager.security.encrypt.ExportEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AesExportEncryptor implements ExportEncryptor {
    private final AesCryptoService aesCryptoService;

    @Override
    public EncryptedExportContainer encrypt(byte[] plainTextBytes, char[] password) {
        byte[] salt = aesCryptoService.generateSalt();
        byte[] iv = aesCryptoService.generateIV();

        SecretKey secretKey = aesCryptoService.generateKey(password, salt);

        byte[] cipherTextBytes = aesCryptoService.encrypt(plainTextBytes, secretKey, iv);

        return new EncryptedExportContainer(
                "0.0.1",
                ExportEncryptorAlgorithm.AES_GCM_256.name(),
                "PBKDF2WithHmacSHA256",
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(cipherTextBytes)
        );
    }

    @Override
    public ExportEncryptorAlgorithm getExportAlgorithm() {
        return ExportEncryptorAlgorithm.AES_GCM_256;
    }
}
