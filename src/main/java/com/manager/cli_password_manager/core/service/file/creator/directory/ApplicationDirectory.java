package com.manager.cli_password_manager.core.service.file.creator.directory;

import com.manager.cli_password_manager.core.exception.Initialization.InitializerException;
import com.manager.cli_password_manager.core.exception.file.loader.FileCreatorException;
import com.manager.cli_password_manager.core.service.file.creator.SecureDirectoryCreator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RequiredArgsConstructor
@Component("applicationDirectoryProvider")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApplicationDirectory implements ApplicationDirectoryManager {
    @Value("${shell.file.userHome}")
    private String userHome;
    @Value("${shell.file.rootDirectory}")
    private String directoryName;

    private final SecureDirectoryCreator directoryCreator;
    private Path applicationDirectory;

    @PostConstruct
    public void init() {
        String homePath = System.getProperty(userHome);
        applicationDirectory = Paths.get(homePath, directoryName);

        log.info("Initializing application directory at: {}", applicationDirectory);
        create();
    }

    @Override
    public void create() {
        if(!isApplicationDirectoryExist()) {
            try {
                directoryCreator.createAndSecure(applicationDirectory);
                log.info("Application directory created successfully.");
            } catch (FileCreatorException e) {
                log.error("Failed to create application directory", e);
                throw new InitializerException("Не удалось создать директорию для данных приложения: " + e.getMessage(), e);
            }
        } else {
            log.info("Application directory already exist.");
        }
    }

    @Override
    public Path getApplicationDirectory() {
        return applicationDirectory;
    }

    @Override
    public boolean isApplicationDirectoryExist() {
        return Files.exists(applicationDirectory);
    }
}
