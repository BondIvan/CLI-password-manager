package com.manager.cli_password_manager.security.encrypt.aes;

import com.manager.cli_password_manager.core.entity.dto.export.EncryptedExportContainer;
import com.manager.cli_password_manager.core.entity.enums.IOEncryptorAlgorithm;
import com.manager.cli_password_manager.core.exception.IO.IngestionFileProtectedException;
import com.manager.cli_password_manager.core.exception.security.CryptoAesOperationException;
import com.manager.cli_password_manager.security.encrypt.IOEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.AEADBadTagException;
import javax.crypto.SecretKey;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AesIOEncryptor implements IOEncryptor {
    private final AesCryptoService aesCryptoService;

    @Override
    public EncryptedExportContainer encrypt(byte[] plainTextBytes, char[] password) {
        byte[] salt = aesCryptoService.generateSalt();
        byte[] iv = aesCryptoService.generateIV();

        SecretKey secretKey = aesCryptoService.generateKey(password, salt);

        byte[] cipherTextBytes = aesCryptoService.encrypt(plainTextBytes, secretKey, iv);

        return new EncryptedExportContainer(
                "0.0.1",
                IOEncryptorAlgorithm.AES_GCM_256.name(),
                "PBKDF2WithHmacSHA256",
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(cipherTextBytes)
        );
    }

    public byte[] decrypt(String cipherText, String salt, String iv, char[] password) {
        try {
            byte[] saltFromFile = Base64.getDecoder().decode(salt);
            byte[] ivFromFile = Base64.getDecoder().decode(iv);
            byte[] cipherBytesTextFromFile = Base64.getDecoder().decode(cipherText);
            SecretKey secretKey = aesCryptoService.generateKey(password, saltFromFile);

            return aesCryptoService.decrypt(cipherBytesTextFromFile, secretKey, ivFromFile);
        } catch (CryptoAesOperationException e) {
            Throwable cause = e.getCause();
            if(cause instanceof AEADBadTagException)
                throw new IngestionFileProtectedException();

            throw e;
        }
    }

    @Override
    public IOEncryptorAlgorithm getExportAlgorithm() {
        return IOEncryptorAlgorithm.AES_GCM_256;
    }
}
