package com.manager.cli_password_manager.core.repository;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.exception.repository.InMemoryRepositoryException;
import com.manager.cli_password_manager.core.service.file.loader.AccessLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class InMemoryNotesRepository implements NoteRepository {
    private final AccessLoader accessLoader;
    private Map<String, List<Note>> notes;

    public InMemoryNotesRepository(AccessLoader accessLoader) {
        this.accessLoader = accessLoader;
        notes = new HashMap<>();
    }

    public void initialize() {
        this.notes = accessLoader.loadAccess();
    }

    @Override
    public void addNote(Note newNote) {
        if(newNote == null) {
            log.warn("Cannot add newNote as null");
            throw new InMemoryRepositoryException("Cannot add null");
        }

        notes.computeIfAbsent(newNote.getName().toLowerCase(), k -> new ArrayList<>()).add(newNote);
    }

    public void update(Map<String, List<Note>> m) {
        this.notes = new HashMap<>(m);
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
        deleteNote(oldNote);
        addNote(newNote);
    }

    @Override
    public void deleteNote(Note deletedNote) {
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
    }

    @Override
    public Map<String, List<Note>> getAllNotes() {
        return Collections.unmodifiableMap(notes);
    }
}
