package com.manager.cli_password_manager.core.service.command;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.command.InputAddDTO;
import com.manager.cli_password_manager.core.entity.dto.command.InputReplaceDTO;
import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;
import com.manager.cli_password_manager.core.entity.enums.IngestionResult;

/**
 * A service that changes the state of data.
 * Depending on the chosen storage, a specific implementation is used.
 * @see FileBasedNoteService
 * @see DbBasedNoteService
 */

public interface NoteService {
    Note createNote(InputAddDTO inputAddDTO);
    boolean deleteNote(NoteNamePlusLoginDTO inputNameLoginDto);
    boolean replaceNote(InputReplaceDTO inputReplaceDTO);
    IngestionResult ingestion(String filePath, String password);
}
