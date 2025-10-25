package com.manager.cli_password_manager.core.service.file.loader;

import com.manager.cli_password_manager.core.exception.Initialization.InitializerException;
import com.manager.cli_password_manager.core.exception.file.loader.FileLoaderException;
import com.manager.cli_password_manager.core.service.file.creator.SecureFileCreator;
import com.manager.cli_password_manager.core.service.file.creator.directory.ApplicationDirectoryManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Slf4j
@Component
@DependsOn("applicationDirectoryProvider")
@RequiredArgsConstructor
public class KeyStoreLoader {
    private static final String KEYSTORE_TYPE = "PKCS12";

    @Value("${shell.file.keyStoreFile}")
    private String vaultFileName;

    private Path vaultPathFile;

    private final SecureFileCreator fileCreator;
    private final ApplicationDirectoryManager directoryManager;

    @PostConstruct
    public void init() {
        try {
            if(!directoryManager.isApplicationDirectoryExist())
                throw new FileLoaderException("Application directory not found.");

            Path dirPath = directoryManager.getApplicationDirectory();
            this.vaultPathFile = dirPath.resolve(vaultFileName);
        } catch (Exception e) {
            log.error("Failed to initialize vault loader service: {}", e.getMessage());
            throw new InitializerException("Failed to initialize vault loader service: " + e.getMessage());
        }
    }

    public KeyStore loadKeyStore(char[] password) {
        log.info("Unlocking the vault");
        KeyStore keyStoreInstance;
        try {
            keyStoreInstance = KeyStore.getInstance(KEYSTORE_TYPE);
            if (Files.exists(vaultPathFile)) {
                try (FileInputStream fis = new FileInputStream(vaultPathFile.toFile())) {
                    keyStoreInstance.load(fis, password);
                }
            } else {
                // If the keyStore has not been created yet, create it empty
                keyStoreInstance.load(null, password);
                fileCreator.createAndSecure(vaultPathFile);
                saveKeyStore(keyStoreInstance, password);
            }
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new FileLoaderException("Ошибка инициализации KeyStore: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new FileLoaderException("Ошибка чтения/загрузки файла KeyStore (возможно, неверный мастер-пароль или файл поврежден): " + vaultPathFile, e);
        }
        return keyStoreInstance;
    }

    public void saveKeyStore(KeyStore keyStore, char[] password) {
        saveKeyStoreToFile(keyStore, password, this.vaultPathFile);
    }

    public void saveKeyStoreToFile(KeyStore keyStore, char[] password, Path path) {
        try(FileOutputStream fos = new FileOutputStream(path.toFile())) {
            keyStore.store(fos, password);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new FileLoaderException("Ошибка сохранения файла KeyStore: " + path, e);
        }
    }

    public Path getVaultPathFile() {
        return vaultPathFile;
    }
}
