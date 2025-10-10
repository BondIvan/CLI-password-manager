package com.manager.cli_password_manager.core.service.command.usecase.add;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.command.InputAddDTO;
import com.manager.cli_password_manager.core.exception.command.AddCommandException;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import com.manager.cli_password_manager.core.repository.InMemoryVaultRepository;
import com.manager.cli_password_manager.core.service.clipboard.ClipboardService;
import com.manager.cli_password_manager.core.service.file.saver.StorageManager;
import com.manager.cli_password_manager.core.service.password.PasswordGenerator;
import com.manager.cli_password_manager.core.service.password.PasswordValidation;
import com.manager.cli_password_manager.core.service.vault.impl.VaultStateService;
import com.manager.cli_password_manager.security.encrypt.PasswordEncryptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AddCommand {
    private final InMemoryNotesRepository notesRepository;
    private final InMemoryVaultRepository vaultRepository;
    @Qualifier("aesPasswordEncryptor")
    private final PasswordEncryptor passwordEncryptor;
    private final PasswordValidation passwordValidation;
    private final PasswordGenerator passwordGenerator;
    private final ClipboardService clipboardService;
    private final StorageManager storageManager;
    private final VaultStateService vaultStateService;

    public AddCommand(
            InMemoryNotesRepository notesRepository,
            InMemoryVaultRepository vaultRepository,
            PasswordValidation validation,
            PasswordGenerator generator,
            PasswordEncryptor passwordEncryptor,
            ClipboardService clipboardService,
            StorageManager storageManager,
            VaultStateService vaultStateService) {
        this.notesRepository = notesRepository;
        this.vaultRepository = vaultRepository;
        this.passwordValidation = validation;
        this.passwordGenerator = generator;
        this.passwordEncryptor = passwordEncryptor;
        this.clipboardService = clipboardService;
        this.storageManager = storageManager;
        this.vaultStateService = vaultStateService;
    }

    public boolean execute(InputAddDTO inputAddDTO) {
        Optional<List<Note>> optionalNotes = notesRepository.findNotesByServiceName(inputAddDTO.serviceName());
        if(optionalNotes.isPresent()) {
            List<Note> existingNotes = optionalNotes.get();
            boolean isDuplicate = existingNotes.stream()
                    .anyMatch(nt -> nt.getLogin().equalsIgnoreCase(inputAddDTO.login()));

            if(isDuplicate)
                throw new AddCommandException("У этого сервиса уже есть аккаунт с таким логином");
        }

        String password;
        if(inputAddDTO.isAutoGeneratePassword()) {
            password = inputAddDTO.excludedSymbols() == null ?
                    passwordGenerator.generate() :
                    passwordGenerator.generate(inputAddDTO.excludedSymbols());
        } else {
            password = inputAddDTO.password();
        }

        if(clipboardService.isClipboardAvailable()) {
            clipboardService.copyToClipboard(password);
        } else {
            log.warn("Clipboard is not available in this environment. Use get command to show service data");
        }

        //TODO Вернуть
//        if(!inputAddDTO.isAutoGeneratePassword() && !passwordValidation.isValid(password))
//            throw new AddCommandException("Пароль не соответствует требованиям: " + passwordValidation.getReason());
        
        try {
            Note newNote = new Note();
            newNote.setName(inputAddDTO.serviceName());
            newNote.setLogin(inputAddDTO.login());
            newNote.setPassword(passwordEncryptor.encryptPassword(newNote.getId(), password));
            newNote.setCategory(inputAddDTO.category());
            
            notesRepository.addNote(newNote);
            storageManager.transactionalFilesSave(); // commit

            return true;
        } catch (Exception any) { // rollback
            log.error("Не удалось добавить запись. Все изменения отменяются. Причина: {}", any.getMessage());

            this.rollback();

            throw new AddCommandException("Не удалось добавить запись.", any);
        }
    }

    private void rollback() {
        try {
            log.info("Rollback state in memory after trying to add note");
            vaultRepository.unlockVault(vaultStateService.getVaultPassword());
            notesRepository.initialize();
            log.info("The memory state rollback was successful");
        } catch (Exception e) {
            log.error("Error rollback adding transaction");
            throw new RuntimeException("Trouble: error rollback adding transaction");
        }
    }
}
