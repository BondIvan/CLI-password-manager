package com.manager.cli_password_manager.core.service.command.usecase.delete;

import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;
import com.manager.cli_password_manager.core.service.command.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteCommand {
    private final NoteService noteService;

    public boolean execute(NoteNamePlusLoginDTO inputNameLoginDto) {
        boolean result = noteService.deleteNote(inputNameLoginDto);
        log.info("Deleted: {} + {}", inputNameLoginDto.name(), inputNameLoginDto.login());

        return result;
    }
}
