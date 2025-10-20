package com.manager.cli_password_manager.core.entity.mapper;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.command.DecryptedNoteDTO;
import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;
import com.manager.cli_password_manager.core.entity.dto.io.NoteExportDTO;
import com.manager.cli_password_manager.security.encrypt.PasswordEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {
    @Qualifier("aesPasswordEncryptor")
    private final PasswordEncryptor passwordEncryptor;

    public NoteMapper(PasswordEncryptor passwordEncryptor) {
        this.passwordEncryptor = passwordEncryptor;
    }

    public DecryptedNoteDTO toDecryptedDto(Note note) {
        return new DecryptedNoteDTO(
                note.getName(),
                note.getLogin(),
                passwordEncryptor.decryptPassword(note.getId(), note.getPassword()),
                note.getCategory()
        );
    }

    public NoteNamePlusLoginDTO toNameLoginDto(Note note) {
        return new NoteNamePlusLoginDTO(
                note.getName(),
                note.getLogin()
        );
    }

    public NoteExportDTO toExportDTO(Note note) {
        return new NoteExportDTO(
                note.getName(),
                note.getLogin(),
                note.getCategory().name(),
                passwordEncryptor.decryptPassword(note.getId(), note.getPassword())
        );
    }
}
