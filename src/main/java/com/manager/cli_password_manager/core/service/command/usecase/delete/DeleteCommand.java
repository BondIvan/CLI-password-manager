package com.manager.cli_password_manager.core.service.command.usecase.delete;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;
import com.manager.cli_password_manager.core.exception.command.DeleteCommandException;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import com.manager.cli_password_manager.core.repository.InMemoryVaultRepository;
import com.manager.cli_password_manager.core.service.file.saver.StorageManager;
import com.manager.cli_password_manager.core.service.vault.impl.VaultStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DeleteCommand {
    private final InMemoryNotesRepository notesRepository;
    private final InMemoryVaultRepository vaultRepository;
    private final StorageManager storageManager;
    private final VaultStateService vaultStateService;

    public DeleteCommand(InMemoryNotesRepository notesRepository,
                         InMemoryVaultRepository vaultRepository,
                         StorageManager storageManager,
                         VaultStateService vaultStateService
    ) {
        this.notesRepository = notesRepository;
        this.vaultRepository = vaultRepository;
        this.storageManager = storageManager;
        this.vaultStateService = vaultStateService;
    }

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

        try {
            vaultRepository.deleteKey(deleting.getId());
            notesRepository.deleteNote(deleting);

            storageManager.transactionalFilesSave();

            return true;
        } catch (Exception any) { // rollback
            log.error("Не удалось удалить запись. Все изменения отменяются. Причина: {}", any.getMessage());

            this.rollback();

            throw new DeleteCommandException("Не удалось добавить запись.", any);
        }
    }

    private void rollback() {
        try {
            log.info("Rollback state in memory after trying to delete note");
            vaultRepository.unlockVault(vaultStateService.getVaultPassword());
            notesRepository.initialize();
            log.info("The memory state rollback was successful");
        } catch (Exception e) {
            log.error("Error rollback deleting transaction");
            throw new RuntimeException("Trouble: error rollback deleting transaction");
        }
    }
}
