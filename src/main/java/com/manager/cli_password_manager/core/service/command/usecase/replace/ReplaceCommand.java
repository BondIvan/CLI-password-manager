package com.manager.cli_password_manager.core.service.command.usecase.replace;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.command.InputReplaceDTO;
import com.manager.cli_password_manager.core.entity.enums.ReplaceType;
import com.manager.cli_password_manager.core.exception.command.ReplaceCommandException;
import com.manager.cli_password_manager.core.exception.command.ReplaceValidationException;
import com.manager.cli_password_manager.core.repository.NoteRepository;
import com.manager.cli_password_manager.core.service.annotation.filetransaction.FileTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplaceCommand {
    private final NoteRepository notesRepository;
    private final Map<ReplaceType, Replacement> replaceManager;

    @FileTransaction(name = "replace note transaction",
            noRollbackFor = {ReplaceCommandException.class, ReplaceValidationException.class})
    public boolean execute(InputReplaceDTO inputReplaceDTO) {
        Optional<List<Note>> optionalNotes = notesRepository.findNotesByServiceName(inputReplaceDTO.serviceName());

        if(optionalNotes.isEmpty())
            throw new ReplaceCommandException("Note with such name not found");

        List<Note> searchedNotes = optionalNotes.get();

        if(searchedNotes.size() > 1 && inputReplaceDTO.login() == null)
            throw new ReplaceCommandException("You have several services with such name. " +
                    "You can find out more details using the 'get' command");

        Note replacingNote;
        if(searchedNotes.size() == 1)
            replacingNote = searchedNotes.getFirst();
        else {
            replacingNote = searchedNotes.stream()
                    .filter(nt -> inputReplaceDTO.login().equalsIgnoreCase(nt.getLogin()))
                    .findFirst()
                    .orElseThrow(() -> new ReplaceCommandException("Cannot find note with such name and login"));
        }

        Replacement replacement = replaceManager.get(inputReplaceDTO.type());

        if(replacement == null)
            throw new ReplaceCommandException("replacement for this type [" + inputReplaceDTO.type().getTitle() + "] note found");

        Note replaced = replacement.replace(replacingNote, inputReplaceDTO.value());
        notesRepository.updateNote(replacingNote, replaced);

        return true;
    }
}
