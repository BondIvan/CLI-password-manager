package com.manager.cli_password_manager.core.service.export;

import com.manager.cli_password_manager.core.entity.dto.export.EncryptedExportContainer;
import com.manager.cli_password_manager.core.entity.enums.ExportEncryptorAlgorithm;
import com.manager.cli_password_manager.core.exception.export.ExportException;
import com.manager.cli_password_manager.security.encrypt.ExportEncryptor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExportEncryptionService {
    private final Map<ExportEncryptorAlgorithm, ExportEncryptor> encryptors;

    public ExportEncryptionService(List<ExportEncryptor> encryptorList) {
        this.encryptors = encryptorList.stream()
                .collect(Collectors.toMap(
                        ExportEncryptor::getExportAlgorithm,
                        Function.identity()
                ));
    }

    public EncryptedExportContainer exportData(byte[] data, ExportEncryptorAlgorithm algorithm, char[] password) {
        ExportEncryptor encryptor = encryptors.get(algorithm);
        if(encryptor == null) {
            throw new ExportException("Unsupported encryption algorithm: " + algorithm);
        }

        return encryptor.encrypt(data, password);
    }
}
