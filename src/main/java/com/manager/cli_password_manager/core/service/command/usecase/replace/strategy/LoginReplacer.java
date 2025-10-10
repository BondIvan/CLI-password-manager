package com.manager.cli_password_manager.core.service.command.usecase.replace.strategy;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.enums.ReplaceType;
import com.manager.cli_password_manager.core.exception.command.ReplaceValidationException;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import com.manager.cli_password_manager.core.service.command.usecase.replace.Replacement;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class LoginReplacer implements Replacement {
    private final InMemoryNotesRepository notesRepository;

    public LoginReplacer(InMemoryNotesRepository notesRepository) {
        this.notesRepository = notesRepository;
    }

    @Override
    public Note replace(Note replacingNote, String newLogin) {
        Optional<List<Note>> optionalNotes = notesRepository.findNotesByServiceName(replacingNote.getName());

        if(optionalNotes.isEmpty())
            throw new ReplaceValidationException("Replacing note does not exist");

        List<Note> servicesWithTheSameName = optionalNotes.get();

        boolean loginAlreadyExist = servicesWithTheSameName.stream()
                .anyMatch(nt -> newLogin.equalsIgnoreCase(nt.getLogin()));

        if(loginAlreadyExist)
            throw new ReplaceValidationException("This service already has an account with this login");

        return replacingNote.withLogin(newLogin);
    }

    @Override
    public ReplaceType getReplaceType() {
        return ReplaceType.LOGIN;
    }
}
