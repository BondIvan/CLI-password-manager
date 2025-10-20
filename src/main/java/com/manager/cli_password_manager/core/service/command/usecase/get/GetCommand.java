package com.manager.cli_password_manager.core.service.command.usecase.get;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.command.DecryptedNoteDTO;
import com.manager.cli_password_manager.core.entity.mapper.NoteMapper;
import com.manager.cli_password_manager.core.exception.command.GetCommandException;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetCommand {
    private final InMemoryNotesRepository notesRepository;
    private final NoteMapper noteMapper;

    public DecryptedNoteDTO getNoteById(String id) {
        return notesRepository.findNoteById(id)
                .map(noteMapper::toDecryptedDto)
                .orElseThrow(() -> new GetCommandException("Note with such id not found"));
    }

    public List<DecryptedNoteDTO> getNotesByName(String name) {
        Optional<List<Note>> optionalNotes = notesRepository.findNotesByServiceName(name);

        if(optionalNotes.isEmpty())
            throw new GetCommandException("Notes with such service name not found");

        return optionalNotes.get().stream()
                .map(noteMapper::toDecryptedDto)
                .toList();
    }
}
