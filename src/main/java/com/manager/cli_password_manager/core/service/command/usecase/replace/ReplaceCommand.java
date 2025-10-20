package com.manager.cli_password_manager.core.service.command.usecase.replace;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.command.InputReplaceDTO;
import com.manager.cli_password_manager.core.entity.enums.ReplaceType;
import com.manager.cli_password_manager.core.exception.command.ReplaceCommandException;
import com.manager.cli_password_manager.core.exception.command.ReplaceValidationException;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import com.manager.cli_password_manager.core.repository.InMemoryVaultRepository;
import com.manager.cli_password_manager.core.service.file.saver.StorageManager;
import com.manager.cli_password_manager.core.service.vault.impl.VaultStateService;
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
    private final InMemoryNotesRepository notesRepository;
    private final InMemoryVaultRepository vaultRepository;
    private final VaultStateService vaultStateService;
    private final StorageManager storageManager;
    private final Map<ReplaceType, Replacement> replaceManager;

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

        try {
            Note replaced = replacement.replace(replacingNote, inputReplaceDTO.value());
            notesRepository.updateNote(replacingNote, replaced);

            storageManager.transactionalFilesSave();

            return true;
        } catch (ReplaceValidationException v) {
            log.warn("Ошибка входных данных длял замены: {}", v.getMessage());
            throw v;
        } catch (Exception any) { // rollback
            log.error("Не удалось изменить запись. Все изменения отменяются. Причина: {}", any.getMessage());

            this.rollback();

            throw new ReplaceCommandException("Не удалось изменить запись.", any);
        }
    }

    private void rollback() {
        try {
            log.info("Rollback state in memory after trying to replcae note");
            vaultRepository.unlockVault(vaultStateService.getVaultPassword());
            notesRepository.initialize();
            log.info("The memory state rollback was successful");
        } catch (Exception e) {
            log.error("Error rollback replace transaction");
            throw new RuntimeException("Trouble: error rollback replacing transaction");
        }
    }
}
