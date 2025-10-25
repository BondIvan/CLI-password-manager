package com.manager.cli_password_manager.core.repository;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.exception.repository.InMemoryRepositoryException;
import com.manager.cli_password_manager.core.service.file.creator.SecureFileCreator;
import com.manager.cli_password_manager.core.service.file.loader.AccessLoader;
import com.manager.cli_password_manager.core.service.annotation.FileTransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class InMemoryNotesRepository implements NoteRepository, FileTransactionRollbackLoading {
    private final AccessLoader accessLoader;
    private final FileTransactionManager fileTransactionManager;
    private final SecureFileCreator fileCreator;

    private Map<String, List<Note>> notes;

    public InMemoryNotesRepository(AccessLoader accessLoader,
                                   FileTransactionManager fileTransactionManager,
                                   SecureFileCreator fileCreator) {
        this.accessLoader = accessLoader;
        this.notes = new HashMap<>();
        this.fileTransactionManager = fileTransactionManager;
        this.fileCreator = fileCreator;
    }

    public void initialize() {
        this.notes = accessLoader.loadAccess();
    }

    @Override
    public void rollbackFileState() {
        log.warn("Rolling back in memory state for note repository");
        initialize();
    }

    public void saveToFile() {
        try {
            Optional<Path> appTmpSavingDir = fileTransactionManager.getCurrentTransactionalDirectory();
            if (appTmpSavingDir.isPresent()) {
                Path tmpSavingDir = appTmpSavingDir.get();
                Path tmpAccessFilePath = fileCreator.createTmpAndSecure(tmpSavingDir, "access-");
                Path originalFilePath = accessLoader.getAccessFilePath();

                accessLoader.saveAccessToFile(notes, tmpAccessFilePath);

                fileTransactionManager.registerFile(originalFilePath, tmpAccessFilePath);

                log.info("Tmp access file created successfully");
            } else {
                throw new RuntimeException("Метод должен быть транзакционным - [FileTransaction]");
            }
        } catch (IOException e) {
            throw new InMemoryRepositoryException("Error saving file: cannot create tmp access file", e);
        }
    }

    @Override
    public void addNote(Note newNote) {
        fileTransactionManager.registerRepoParticipant(this);

        if(newNote == null) {
            log.warn("Cannot add newNote as null");
            throw new InMemoryRepositoryException("Cannot add null");
        }

        notes.computeIfAbsent(newNote.getName().toLowerCase(), k -> new ArrayList<>()).add(newNote);

        this.saveToFile();
    }

    @Override
    public void update(Map<String, List<Note>> m) {
        fileTransactionManager.registerRepoParticipant(this);

        this.notes = new HashMap<>(m);

        this.saveToFile();
    }

    @Override
    public Optional<List<Note>> findNotesByServiceName(String serviceName) {
        List<Note> searchedNotes = notes.get(serviceName.toLowerCase());
        if(searchedNotes == null)
            return Optional.empty();

        return Optional.of(Collections.unmodifiableList(searchedNotes));
    }

    @Override
    public Optional<Note> findNoteById(String id) {
        return notes.values().stream()
                .flatMap(List::stream)
                .filter(nt -> id.equals(nt.getId()))
                .findFirst();
    }

    @Override
    public void updateNote(Note oldNote, Note newNote) {
        fileTransactionManager.registerRepoParticipant(this);

        deleteNote(oldNote);
        addNote(newNote);

        this.saveToFile();
    }

    @Override
    public void deleteNote(Note deletedNote) {
        fileTransactionManager.registerRepoParticipant(this);

        if(deletedNote == null) {
            log.warn("Cannot delete note because it null");
            throw new InMemoryRepositoryException("Cannot delete null");
        }

        List<Note> deletingFrom = notes.get(deletedNote.getName().toLowerCase());

        if(deletingFrom == null) {
            log.warn("Notes with these name not found");
            throw new InMemoryRepositoryException("Notes with these name not found");
        }

        deletingFrom.removeIf(deletedNote::equals);

        if(deletingFrom.isEmpty()) // delete empty list
            notes.remove(deletedNote.getName().toLowerCase());

        this.saveToFile();
    }

    @Override
    public Map<String, List<Note>> getAllNotes() {
        return Collections.unmodifiableMap(notes);
    }
}
