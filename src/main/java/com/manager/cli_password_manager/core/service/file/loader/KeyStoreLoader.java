package com.manager.cli_password_manager.core.service.file.loader;

import com.manager.cli_password_manager.core.exception.file.loader.FileLoaderException;
import com.manager.cli_password_manager.core.service.file.creator.SecureFileCreator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Component
public class KeyStoreLoader {
    private static final String KEYSTORE_TYPE = "PKCS12";

    @Value("${shell.file.userHome}")
    private String userHome;
    @Value("${shell.file.rootDirectory}")
    private String directoryName;
    @Value("${shell.file.keyStoreFile}")
    private String vaultFileName;

    private Path vaultPathFile;

    private final SecureFileCreator fileCreator;

    public KeyStoreLoader(SecureFileCreator fileCreator) {
        this.fileCreator = fileCreator;
    }

    @PostConstruct
    public void init() {
        String homePath = System.getProperty(userHome);
        Path appDataDir = Paths.get(homePath, directoryName);

        if(!Files.exists(appDataDir))
            throw new FileLoaderException("Application directory not found");

        this.vaultPathFile = appDataDir.resolve(vaultFileName);
    }

    public KeyStore loadKeyStore(char[] password) {
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
