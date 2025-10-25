package com.manager.cli_password_manager.core.repository;

import com.manager.cli_password_manager.core.entity.Note;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface NoteRepository {
    void addNote(Note newNote);
    void updateNote(Note oldNote, Note newNote);
    void deleteNote(Note deletedNote);
    void update(Map<String, List<Note>> m);
    Map<String, List<Note>> getAllNotes();
    Optional<Note> findNoteById(String id);
    Optional<List<Note>> findNotesByServiceName(String serviceName);
}
