package com.manager.cli_password_manager.core.service.command.usecase.replace.strategy;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.replacer.ReplacementResult;
import com.manager.cli_password_manager.core.entity.enums.ReplaceType;
import com.manager.cli_password_manager.core.exception.command.ReplaceValidationException;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import com.manager.cli_password_manager.core.service.command.usecase.replace.Replacement;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class NameReplacer implements Replacement {
    private final InMemoryNotesRepository notesRepository;

    public NameReplacer(InMemoryNotesRepository notesRepository) {
        this.notesRepository = notesRepository;
    }

    @Override
    public ReplacementResult replace(Note replacingNote, String newName) {
        if(replacingNote.getName().equalsIgnoreCase(newName)) {
            return new ReplacementResult(
                    replacingNote.withName(newName),
                    Optional.empty()
            );
        }

        Optional<List<Note>> optionalNotesForNewName = notesRepository.findNotesByServiceName(newName);

        if(optionalNotesForNewName.isEmpty()) {
            return new ReplacementResult(
                    replacingNote.withName(newName),
                    Optional.empty()
            );
        }

        List<Note> matchesName = optionalNotesForNewName.get();

        boolean hasMatchingNameServicesTheSameLogin = matchesName.stream()
                .anyMatch(nt -> replacingNote.getLogin().equalsIgnoreCase(nt.getLogin()));

        if(hasMatchingNameServicesTheSameLogin)
            throw new ReplaceValidationException("This service already has an account with this login");

        return new ReplacementResult(
                replacingNote.withName(newName),
                Optional.empty()
        );
    }

    @Override
    public ReplaceType getReplaceType() {
        return ReplaceType.NAME;
    }
}
