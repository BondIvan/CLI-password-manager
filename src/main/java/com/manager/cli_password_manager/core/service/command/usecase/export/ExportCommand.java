package com.manager.cli_password_manager.core.service.command.usecase.export;

import com.manager.cli_password_manager.core.entity.converter.StringExportFormatConverter;
import com.manager.cli_password_manager.core.exception.command.ExportCommandException;
import com.manager.cli_password_manager.core.repository.NoteRepository;
import com.manager.cli_password_manager.core.service.file.creator.SecureFileCreator;
import com.manager.cli_password_manager.core.service.file.creator.directory.ApplicationDirectoryManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportCommand {
    private final NoteRepository notesRepository;
    private final SecureFileCreator fileCreator;
    private final ExportService exportService;
    private final ApplicationDirectoryManager directoryManager;
    private final StringExportFormatConverter stringExportFormatConverter;

    @Value("${shell.export.name}")
    private String exportFileName;

    private Path exportFilePath;

    @PostConstruct
    public void init() {
        Path dirPath = directoryManager.getApplicationDirectory();
        this.exportFilePath = dirPath.resolve(exportFileName);
    }

    public void execute(String format, String passwordProtection) {
        try {
            ExportFormat exportFormat = stringExportFormatConverter.toExportFormat(format);

            ExportContext exportContext = ExportContext.builder()
                .notes(notesRepository.getAllNotes())
                .password(passwordProtection != null ? passwordProtection.toCharArray() : null)
                .build();

            fileCreator.createAndSecure(exportFilePath);
            exportService.exportToFile(exportFilePath, exportFormat, exportContext);
        }catch (IllegalArgumentException | IOException e) {
            log.error("Export failed", e);
            throw new ExportCommandException("Export failed: " + e.getMessage(), e);
        }
    }
}
