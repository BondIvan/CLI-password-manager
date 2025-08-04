package com.manager.cli_password_manager.core.service.MP;

import com.manager.cli_password_manager.core.exception.Initialization.InitializerException;
import com.manager.cli_password_manager.core.exception.file.loader.FileLoaderException;
import com.manager.cli_password_manager.core.exception.security.EncryptionException;
import com.manager.cli_password_manager.core.exception.security.MasterPasswordException;
import com.manager.cli_password_manager.core.service.file.creator.SecureFileCreator;
import com.manager.cli_password_manager.core.service.file.creator.directory.ApplicationDirectoryManager;
import com.manager.cli_password_manager.security.EncryptionUtils;
import com.manager.cli_password_manager.security.encrypt.Encrypting;
import com.manager.cli_password_manager.security.encrypt.aes.AES_GCM;
import com.manager.cli_password_manager.security.hash.Hashing;
import com.manager.cli_password_manager.security.hash.argon2.Argon2;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Slf4j
@Service
@DependsOn("applicationDirectoryProvider")
public class MasterPasswordService {
    @Value("${shell.file.MP}")
    private String masterPasswordFileName; //TODO current - asd

    private Path masterPasswordPathFile;

    private final Encrypting aesGcm;
    private final Hashing argon2;
    private final SecureFileCreator fileCreator;
    private final ApplicationDirectoryManager directoryManager;

    public MasterPasswordService(AES_GCM aesGcm,
                                 Argon2 argon2,
                                 SecureFileCreator fileCreator,
                                 ApplicationDirectoryManager directoryManager) {
        this.aesGcm = aesGcm;
        this.argon2 = argon2;
        this.fileCreator = fileCreator;
        this.directoryManager = directoryManager;
    }

    @PostConstruct
    public void init() {
        try {
            if (!directoryManager.isApplicationDirectoryExist())
                throw new FileLoaderException("Application directory not found.");

            Path dirPath = directoryManager.getApplicationDirectory();
            this.masterPasswordPathFile = dirPath.resolve(masterPasswordFileName);
        } catch (Exception e) {
            log.error("Failed to initialize master password service: {}", e.getMessage());
            throw new InitializerException("Failed to initialize master password service: " + e.getMessage());
        }
    }

    public boolean isExist() {
        return Files.exists(masterPasswordPathFile);
    }

    private boolean checkInputMasterPassword(String inputPassword) {
        byte[] validation;

        try {
            validation = Files.readAllBytes(masterPasswordPathFile);
        } catch (IOException e) {
            throw new MasterPasswordException("Error reading validation files: " + e.getMessage(), e);
        }

        if(validation.length < (AES_GCM.GCM_IV_LENGTH + Argon2.SALT_LENGTH))
            throw new EncryptionException("File damaged");

        byte[] storedArgon2Salt = Arrays.copyOfRange(validation, 0, Argon2.SALT_LENGTH);
        byte[] iv = Arrays.copyOfRange(validation, Argon2.SALT_LENGTH, Argon2.SALT_LENGTH + AES_GCM.GCM_IV_LENGTH);
        byte[] validationWithIv = Arrays.copyOfRange(validation, Argon2.SALT_LENGTH, validation.length);
        byte[] key = argon2.hashWithSalt(inputPassword, storedArgon2Salt);
        byte[] unverified = aesGcm.encryptMasterPassword("validation", key, iv);

        boolean result = Arrays.equals(validationWithIv, unverified);

        EncryptionUtils.clearData(validation, storedArgon2Salt, iv, validationWithIv, unverified);

        return result;
    }

    public boolean verify(String password) {
        return checkInputMasterPassword(password);
    }

    //TODO Message result (сомнительно)
    //TODO Создать требования для мастер-пароля
    public void createMasterPassword(char[] inputPassword) {
        String password = new String(inputPassword);

        byte[] argon2Salt = argon2.generateSalt();
        byte[] aesKey = argon2.hashWithSalt(password, argon2Salt);
        byte[] validation = aesGcm.encryptMasterPassword("validation", aesKey, null);
        byte[] argonSaltPlusValidation = new byte[argon2Salt.length + validation.length];

        System.arraycopy(argon2Salt, 0, argonSaltPlusValidation, 0, argon2Salt.length);
        System.arraycopy(validation, 0, argonSaltPlusValidation, argon2Salt.length, validation.length);

        try {
            fileCreator.createAndSecure(masterPasswordPathFile);
            Files.write(masterPasswordPathFile, argonSaltPlusValidation);
        } catch (IOException e) {
            throw new MasterPasswordException("Writing validation files error: " + e.getMessage(), e);
        } finally {
            EncryptionUtils.clearData(argon2Salt, aesKey, validation, argonSaltPlusValidation);
            EncryptionUtils.clearData(inputPassword);
        }
    }
}
