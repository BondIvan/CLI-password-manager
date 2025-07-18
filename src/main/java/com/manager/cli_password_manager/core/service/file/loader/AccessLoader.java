package com.manager.cli_password_manager.core.service.file.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.exception.file.loader.FileCreatorException;
import com.manager.cli_password_manager.core.exception.file.loader.FileLoaderException;
import com.manager.cli_password_manager.core.service.file.creator.SecureFileCreator;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
public class AccessLoader {
    @Value("${shell.file.userHome}")
    private String userHome;
    @Value("${shell.file.rootDirectory}")
    private String directoryName;
    @Value("${shell.file.accessFile}")
    private  String accessFileName;

    private Path accessFilePath;

    private final ObjectMapper objectMapper;
    private final SecureFileCreator fileCreator;

    public AccessLoader(SecureFileCreator fileCreator, ObjectMapper objectMapper) {
        this.fileCreator = fileCreator;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        String homePath = System.getProperty(userHome);
        Path appDataDir = Paths.get(homePath, directoryName);

        if(!Files.exists(appDataDir))
            throw new FileLoaderException("Application directory not found");

        this.accessFilePath = appDataDir.resolve(accessFileName);
    }

    public Map<String, List<Note>> loadAccess() {
        log.info("Loading data access");
        try {
            fileCreator.createAndSecure(accessFilePath);

            if(accessFilePath.toFile().length() == 0)
                return new TreeMap<>();

            return objectMapper.readValue(accessFilePath.toFile(), new TypeReference<TreeMap<String, List<Note>>>(){});
        } catch (FileCreatorException e) {
            log.error("Cannot create access file: {}", e.getMessage());
            throw new FileLoaderException("Cannot create access file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Load access file error: {}", e.getMessage());
            throw new FileLoaderException("Load access file error: " + e.getMessage(), e);
        }
    }

    public void saveAccess(Map<String, List<Note>> notes) {
        saveAccessToFile(notes, this.accessFilePath);
    }

    public void saveAccessToFile(Map<String, List<Note>> notes, Path path) {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            objectMapper.writeValue(path.toFile(), notes);
        } catch (Exception e) {
            log.error("Save access file error: {}", e.getMessage());
            throw new FileLoaderException("Save access file error: " + e.getMessage(), e);
        }
    }

    public Path getAccessFilePath() {
        return accessFilePath;
    }
}
