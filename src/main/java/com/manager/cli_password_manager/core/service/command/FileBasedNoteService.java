package com.manager.cli_password_manager.core.service.command;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.command.InputAddDTO;
import com.manager.cli_password_manager.core.entity.dto.command.InputReplaceDTO;
import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;
import com.manager.cli_password_manager.core.entity.enums.IngestionResult;
import com.manager.cli_password_manager.core.exception.IO.ingestion.IngestionCommandException;
import com.manager.cli_password_manager.core.exception.IO.ingestion.IngestionException;
import com.manager.cli_password_manager.core.exception.clipboard.ClipboardException;
import com.manager.cli_password_manager.core.exception.command.AddCommandException;
import com.manager.cli_password_manager.core.exception.command.DeleteCommandException;
import com.manager.cli_password_manager.core.exception.command.ReplaceCommandException;
import com.manager.cli_password_manager.core.exception.command.ReplaceValidationException;
import com.manager.cli_password_manager.core.repository.NoteRepository;
import com.manager.cli_password_manager.core.service.annotation.filetransaction.FileTransaction;
import com.manager.cli_password_manager.core.service.command.usecase.add.CreateNoteUseCase;
import com.manager.cli_password_manager.core.service.command.usecase.delete.DeleteNoteUseCase;
import com.manager.cli_password_manager.core.service.command.usecase.ingestion.IngestionUseCase;
import com.manager.cli_password_manager.core.service.command.usecase.replace.ReplaceNoteUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("file-storage")
public class FileBasedNoteService implements NoteService {
    private final CreateNoteUseCase createNoteUseCase;
    private final DeleteNoteUseCase deleteNoteUseCase;
    private final ReplaceNoteUseCase replaceNoteUseCase;
    private final IngestionUseCase ingestionUseCase;

    @Qualifier("memoryNotesRepository")
    private final NoteRepository noteRepository;

    public FileBasedNoteService(CreateNoteUseCase createNoteUseCase,
                                DeleteNoteUseCase deleteNoteUseCase,
                                NoteRepository noteRepository,
                                ReplaceNoteUseCase replaceNoteUseCase,
                                IngestionUseCase ingestionUseCase) {
        this.createNoteUseCase = createNoteUseCase;
        this.deleteNoteUseCase = deleteNoteUseCase;
        this.replaceNoteUseCase = replaceNoteUseCase;
        this.noteRepository = noteRepository;
        this.ingestionUseCase = ingestionUseCase;
    }

    @Override
    @FileTransaction(name = "add note transaction", noRollbackFor = {AddCommandException.class, ClipboardException.class})
    public Note createNote(InputAddDTO inputAddDTO) {
        Note newNote = createNoteUseCase.create(inputAddDTO);
        noteRepository.addNote(newNote);

        return newNote;
    }

    @Override
    @FileTransaction(name = "delete note transaction", noRollbackFor = DeleteCommandException.class)
    public boolean deleteNote(NoteNamePlusLoginDTO inputNameLoginDto) {
        return deleteNoteUseCase.delete(inputNameLoginDto);
    }

    @Override
    @FileTransaction(name = "replace note transaction",
            noRollbackFor = {ReplaceCommandException.class, ReplaceValidationException.class})
    public boolean replaceNote(InputReplaceDTO inputReplaceDTO) {
        return replaceNoteUseCase.replace(inputReplaceDTO);
    }

    @Override
    @FileTransaction(name = "import note transaction",
            noRollbackFor = {IngestionCommandException.class, IngestionException.class})
    public IngestionResult ingestion(String filePath, String password) {
        return ingestionUseCase.ingestion(filePath, password);
    }
}
