package com.manager.cli_password_manager.core.entity.mapper;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.command.DecryptedNoteDTO;
import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;
import com.manager.cli_password_manager.security.encrypt.aes.AesPasswordEncryptor;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {
    private final AesPasswordEncryptor aesPasswordEncryptor;

    public NoteMapper(AesPasswordEncryptor aesPasswordEncryptor) {
        this.aesPasswordEncryptor = aesPasswordEncryptor;
    }

    public DecryptedNoteDTO toDecryptedDto(Note note) {
        return new DecryptedNoteDTO(
                note.getName(),
                note.getLogin(),
                aesPasswordEncryptor.decryptPassword(note.getId(), note.getPassword()),
                note.getCategory()
        );
    }

    public NoteNamePlusLoginDTO toNameLoginDto(Note note) {
        return new NoteNamePlusLoginDTO(
                note.getName(),
                note.getLogin()
        );
    }
}
