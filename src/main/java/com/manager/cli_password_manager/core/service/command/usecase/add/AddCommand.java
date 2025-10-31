package com.manager.cli_password_manager.core.service.command.usecase.add;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.command.InputAddDTO;
import com.manager.cli_password_manager.core.exception.clipboard.ClipboardException;
import com.manager.cli_password_manager.core.exception.command.AddCommandException;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import com.manager.cli_password_manager.core.service.annotation.filetransaction.FileTransaction;
import com.manager.cli_password_manager.core.service.clipboard.ClipboardService;
import com.manager.cli_password_manager.core.service.password.PasswordGenerator;
import com.manager.cli_password_manager.core.service.password.PasswordValidation;
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
    @Qualifier("aesPasswordEncryptor")
    private final PasswordEncryptor passwordEncryptor;
    private final PasswordValidation passwordValidation;
    private final PasswordGenerator passwordGenerator;
    private final ClipboardService clipboardService;

    public AddCommand(
            InMemoryNotesRepository notesRepository,
            PasswordValidation validation,
            PasswordGenerator generator,
            PasswordEncryptor passwordEncryptor,
            ClipboardService clipboardService) {
        this.notesRepository = notesRepository;
        this.passwordValidation = validation;
        this.passwordGenerator = generator;
        this.passwordEncryptor = passwordEncryptor;
        this.clipboardService = clipboardService;
    }

    @FileTransaction(name = "add note transaction",
            noRollbackFor = {AddCommandException.class, ClipboardException.class})
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

        Note newNote = new Note();
        newNote.setName(inputAddDTO.serviceName());
        newNote.setLogin(inputAddDTO.login());
        newNote.setPassword(passwordEncryptor.encryptPassword(newNote.getId(), password));
        newNote.setCategory(inputAddDTO.category());

        notesRepository.addNote(newNote);

        return true;
    }
}
