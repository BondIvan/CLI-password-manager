package com.manager.cli_password_manager.core.service.command.usecase.replace.strategy;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.enums.ReplaceType;
import com.manager.cli_password_manager.core.exception.command.ReplaceValidationException;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import com.manager.cli_password_manager.core.service.command.usecase.replace.Replacement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NameReplacer implements Replacement {
    private final InMemoryNotesRepository notesRepository;

    @Override
    public Note replace(Note replacingNote, String newName) {
        if(replacingNote.getName().equalsIgnoreCase(newName))
            return replacingNote.withName(newName);

        Optional<List<Note>> optionalNotesForNewName = notesRepository.findNotesByServiceName(newName);

        if(optionalNotesForNewName.isEmpty())
            return replacingNote.withName(newName);

        List<Note> matchesName = optionalNotesForNewName.get();

        boolean hasMatchingNameServicesTheSameLogin = matchesName.stream()
                .anyMatch(nt -> replacingNote.getLogin().equalsIgnoreCase(nt.getLogin()));

        if(hasMatchingNameServicesTheSameLogin)
            throw new ReplaceValidationException("This service already has an account with this login");

        return replacingNote.withName(newName);
    }

    @Override
    public ReplaceType getReplaceType() {
        return ReplaceType.NAME;
    }
}
