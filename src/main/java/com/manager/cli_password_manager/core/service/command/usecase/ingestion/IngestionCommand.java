package com.manager.cli_password_manager.core.service.command.usecase.ingestion;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.enums.IngestionFormat;
import com.manager.cli_password_manager.core.exception.IO.IngestionCommandException;
import com.manager.cli_password_manager.core.exception.IO.IngestionException;
import com.manager.cli_password_manager.core.exception.IO.IngestionFileProtectedException;
import com.manager.cli_password_manager.core.exception.repository.StorageManagerException;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import com.manager.cli_password_manager.core.repository.InMemoryVaultRepository;
import com.manager.cli_password_manager.core.service.file.saver.StorageManager;
import com.manager.cli_password_manager.core.service.vault.impl.VaultStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionCommand {
    private final InMemoryNotesRepository notesRepository;
    private final IngestionService ingestionService;
    private final IngestionMerge ingestionMerge;
    private final StorageManager storageManager;

    private final InMemoryVaultRepository vaultRepository;
    private final VaultStateService vaultStateService;

    public void execute(String filePath, String password) {
        // is file available
        // has file permissions

        Path path = Paths.get(filePath);
        if(!Files.exists(path))
            throw new IngestionCommandException("Cannot find the file");

        IngestionContext ingestionContext = new IngestionContext(
                path,
                password
        );

        try {
            IngestionFormat ingestionFormat = ingestionService.getImportFormatFromFileExtension(path);

            Map<String, List<Note>> imported = ingestionService.importFromFile(ingestionFormat, ingestionContext);
            Map<String, List<Note>> notes = notesRepository.getAllNotes();
            Map<String, List<Note>> copy = new HashMap<>(notes);

            ingestionMerge.merge(copy, imported);
            notesRepository.addAll(copy);

            storageManager.transactionalFilesSave();

        } catch (IngestionFileProtectedException e) {
            throw new IngestionCommandException(e);
        } catch (IngestionException e) {
            throw new IngestionCommandException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IngestionCommandException("Unknown import format");
        } catch (IOException e) {
            throw new IngestionCommandException("Import failed: " + e.getMessage(), e);
        } catch (StorageManagerException e) {
            log.error("Не удалось импортировать данные.");
            this.rollback();
            throw new IngestionCommandException("Import failed by reason: cannot save files", e);
        }
    }

    private void rollback() {
        try {
            log.info("Rollback state in memory after trying to add note");
            vaultRepository.unlockVault(vaultStateService.getVaultPassword());
            notesRepository.initialize();
            log.info("The memory state rollback was successful");
        } catch (Exception e) {
            log.error("Error rollback adding transaction");
            throw new RuntimeException("Trouble: error rollback adding transaction");
        }
    }
}
