package com.manager.cli_password_manager.core.service.command;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.command.InputAddDTO;
import com.manager.cli_password_manager.core.entity.dto.command.InputReplaceDTO;
import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;
import com.manager.cli_password_manager.core.entity.enums.IngestionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("db-storage")
public class DbBasedNoteService implements NoteService {
    @Override
    //@Transactional(noRollbackFor = )
    public Note createNote(InputAddDTO inputAddDTO) {
        return null;
    }

    @Override
    //@Transactional(noRollbackFor = )
    public boolean deleteNote(NoteNamePlusLoginDTO inputNameLoginDto) {
        return false;
    }

    @Override
    //@Transactional(noRollbackFor = )
    public boolean replaceNote(InputReplaceDTO inputReplaceDTO) {
        return false;
    }

    @Override
    //@Transactional(noRollbackFor = )
    public IngestionResult ingestion(String filePath, String password) {
        return null;
    }
}
