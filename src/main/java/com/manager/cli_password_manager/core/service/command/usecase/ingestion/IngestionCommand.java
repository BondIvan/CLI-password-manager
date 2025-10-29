package com.manager.cli_password_manager.core.service.command.usecase.ingestion;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.io.MergeResult;
import com.manager.cli_password_manager.core.entity.enums.IngestionFormat;
import com.manager.cli_password_manager.core.entity.enums.IngestionResult;
import com.manager.cli_password_manager.core.exception.IO.IngestionCommandException;
import com.manager.cli_password_manager.core.exception.IO.IngestionException;
import com.manager.cli_password_manager.core.exception.IO.IngestionFileProtectedException;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import com.manager.cli_password_manager.core.repository.InMemoryVaultRepository;
import com.manager.cli_password_manager.core.service.annotation.FileTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final InMemoryVaultRepository vaultRepository;
    private final IngestionService ingestionService;
    private final IngestionMerge ingestionMerge;

    @FileTransaction(name = "import note transaction",
            noRollbackFor = {IngestionCommandException.class, IngestionException.class})
    public IngestionResult execute(String filePath, String password) {
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
        } catch (IngestionFileProtectedException e) {
            return IngestionResult.PASSWORD_REQUIRED;
        }

        Map<String, List<Note>> notes = notesRepository.getAllNotes();
        Map<String, List<Note>> copy = new HashMap<>(notes);

        MergeResult mergeResult = ingestionMerge.merge(copy, imported);
        notesRepository.update(mergeResult.merged());
        mergeResult.replacedNoteIds().forEach(vaultRepository::deleteKey);

        return IngestionResult.SUCCESS;
    }
}
