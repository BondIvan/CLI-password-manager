package com.manager.cli_password_manager.core.service.command.usecase.delete;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;
import com.manager.cli_password_manager.core.exception.command.DeleteCommandException;
import com.manager.cli_password_manager.core.repository.InMemoryVaultRepository;
import com.manager.cli_password_manager.core.repository.NoteRepository;
import com.manager.cli_password_manager.core.service.annotation.filetransaction.FileTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteCommand {
    private final NoteRepository notesRepository; //TODO Тут должен быть интерфейс NoteRepository
    private final InMemoryVaultRepository vaultRepository;

    @FileTransaction(name = "delete note transaction", noRollbackFor = DeleteCommandException.class)
    public boolean execute(NoteNamePlusLoginDTO inputNameLoginDto) {
        Optional<List<Note>> notesByName = notesRepository.findNotesByServiceName(inputNameLoginDto.name());

        if(notesByName.isEmpty())
            throw new DeleteCommandException("Note with such name not found");

        List<Note> searchedNotes = notesByName.get();

        if(searchedNotes.size() > 1 && inputNameLoginDto.login() == null)
            throw new DeleteCommandException("You have several services with such name. " +
                    "You can find out more details using the 'get' command");

        Note deleting;
        if(searchedNotes.size() == 1) {
            deleting = searchedNotes.getFirst();
        } else {
            deleting = searchedNotes.stream()
                    .filter(nt -> inputNameLoginDto.login().equalsIgnoreCase(nt.getLogin()))
                    .findFirst()
                    .orElseThrow(() -> new DeleteCommandException("Cannot find note with such name and login"));
        }

        vaultRepository.deleteKey(deleting.getId());
        notesRepository.deleteNote(deleting);

        return true;
    }
}
