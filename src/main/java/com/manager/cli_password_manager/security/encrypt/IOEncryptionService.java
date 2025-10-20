package com.manager.cli_password_manager.security.encrypt;

import com.manager.cli_password_manager.core.entity.dto.io.EncryptedExportContainer;
import com.manager.cli_password_manager.core.entity.enums.IOEncryptorAlgorithm;
import com.manager.cli_password_manager.core.exception.export.ExportException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class IOEncryptionService {
    private final Map<IOEncryptorAlgorithm, IOEncryptor> encryptors;

    public EncryptedExportContainer exportData(byte[] data, IOEncryptorAlgorithm algorithm, char[] password) {
        IOEncryptor encryptor = encryptors.get(algorithm);
        if(encryptor == null) {
            throw new ExportException("Unsupported encryption algorithm: " + algorithm);
        }

        return encryptor.encrypt(data, password);
    }

    public byte[] importData(String data, String salt, String iv, IOEncryptorAlgorithm algorithm, char[] password) {
        IOEncryptor decryptor = encryptors.get(algorithm);
        if(decryptor == null) {
            throw new ExportException("Unsupported decryption algorithm: " + algorithm);
        }

        return decryptor.decrypt(data, salt, iv, password);
    }
}
