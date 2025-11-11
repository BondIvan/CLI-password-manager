package com.manager.cli_password_manager.core.service.command.usecase.ingestion;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.io.MergeResult;
import com.manager.cli_password_manager.core.entity.enums.IngestionFormat;
import com.manager.cli_password_manager.core.entity.enums.IngestionResult;
import com.manager.cli_password_manager.core.exception.IO.ingestion.IngestionCommandException;
import com.manager.cli_password_manager.core.exception.IO.ingestion.IngestionException;
import com.manager.cli_password_manager.core.exception.IO.ingestion.IngestionFileProtectedException;
import com.manager.cli_password_manager.core.repository.InMemoryVaultRepository;
import com.manager.cli_password_manager.core.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestionUseCase {
    private final NoteRepository noteRepository;
    private final InMemoryVaultRepository vaultRepository;
    private final IngestionService ingestionService;
    private final IngestionMerge ingestionMerge;

    public IngestionResult ingestion(String filePath, String password) {
        Path path = Paths.get(filePath);
        if(!Files.isRegularFile(path))
            throw new IngestionCommandException("Cannot find the file");

        if(!Files.isReadable(path))
            throw new IngestionCommandException("Cannot read the file. Check permissions");

        IngestionContext ingestionContext = new IngestionContext(path, password);
        IngestionFormat ingestionFormat;
        Map<String, List<Note>> imported;

        try {
            ingestionFormat = ingestionService.getImportFormatFromFileExtension(path);
            imported = ingestionService.importFromFile(ingestionFormat, ingestionContext);
        } catch (IngestionException e) {
            if(e.getCause() instanceof IngestionFileProtectedException) {
                log.warn("Incorrect password for the imported file");
                return IngestionResult.PASSWORD_REQUIRED;
            }

            log.error("Import failed", e);
            throw new IngestionCommandException("Import failed", e);
        }

        Map<String, List<Note>> notes = noteRepository.getAllNotes();
        Map<String, List<Note>> copy = new HashMap<>(notes);

        MergeResult mergeResult = ingestionMerge.merge(copy, imported);
        noteRepository.update(mergeResult.merged());
        mergeResult.replacedNoteIds().forEach(vaultRepository::deleteKey);

        return IngestionResult.SUCCESS;
    }
}
