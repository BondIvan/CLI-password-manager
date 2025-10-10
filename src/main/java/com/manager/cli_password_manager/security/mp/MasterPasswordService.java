package com.manager.cli_password_manager.security.mp;

import com.manager.cli_password_manager.core.exception.Initialization.InitializerException;
import com.manager.cli_password_manager.core.exception.file.loader.FileLoaderException;
import com.manager.cli_password_manager.core.service.file.creator.SecureFileCreator;
import com.manager.cli_password_manager.core.service.file.creator.directory.ApplicationDirectoryManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

@Slf4j
@Service
@RequiredArgsConstructor
@DependsOn("applicationDirectoryProvider")
public class MasterPasswordService {
    @Value("${shell.file.MP}")
    private String masterPasswordFileName; //TODO current - asd
    private Path masterPasswordPathFile;

    private final MasterPasswordAssembly masterPasswordAssembly;
    private final ApplicationDirectoryManager directoryManager;
    private final SecureFileCreator fileCreator;

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

    public void create(String password) throws IOException {
        byte[] encodedMasterPassword = masterPasswordAssembly.encodeMasterPassword(password);
        fileCreator.createAndSecure(masterPasswordPathFile);
        Files.write(masterPasswordPathFile, encodedMasterPassword);
    }

    public boolean verify(String password) throws IOException {
        byte[] readBytes = Files.readAllBytes(masterPasswordPathFile);
        byte[] decoded = masterPasswordAssembly.decodeMasterPassword(readBytes, password);

        return MessageDigest.isEqual("validation".getBytes(), decoded);
    }

    public boolean isExist() {
        return Files.exists(masterPasswordPathFile);
    }
}
