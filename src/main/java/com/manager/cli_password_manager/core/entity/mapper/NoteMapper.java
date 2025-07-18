package com.manager.cli_password_manager.core.entity.mapper;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.command.DecryptedNoteDTO;
import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;
import com.manager.cli_password_manager.core.repository.InMemoryVaultRepository;
import com.manager.cli_password_manager.security.encrypt.aes.AES_GCM;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {
    private final AES_GCM aesGcm;
    private final InMemoryVaultRepository vaultRepository;

    public NoteMapper(AES_GCM aesGcm, InMemoryVaultRepository vaultRepository) {
        this.aesGcm = aesGcm;
        this.vaultRepository = vaultRepository;
    }

    public DecryptedNoteDTO toDecryptedDto(Note note) {
        return new DecryptedNoteDTO(
                note.getName(),
                note.getLogin(),
                aesGcm.decryptPassword(vaultRepository.getKey(note.getId()), note.getPassword()),
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
