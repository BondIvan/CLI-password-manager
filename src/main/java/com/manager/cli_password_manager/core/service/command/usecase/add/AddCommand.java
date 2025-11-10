package com.manager.cli_password_manager.core.service.command.usecase.add;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.command.InputAddDTO;
import com.manager.cli_password_manager.core.service.command.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddCommand {
    private final NoteService noteService;

    public boolean execute(InputAddDTO inputAddDTO) {
        Note note = noteService.createNote(inputAddDTO);
        log.info("Created: {}", note.toString());

        return true;
    }
}
