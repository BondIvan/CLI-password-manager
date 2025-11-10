package com.manager.cli_password_manager.core.service.command.usecase.replace;

import com.manager.cli_password_manager.core.entity.dto.command.InputReplaceDTO;
import com.manager.cli_password_manager.core.service.command.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplaceCommand {
    private final NoteService noteService;

    public boolean execute(InputReplaceDTO inputReplaceDTO) {
        boolean result = noteService.replaceNote(inputReplaceDTO);
        log.info("Replaced: {} + {}", inputReplaceDTO.serviceName(), inputReplaceDTO.login());

        return result;
    }
}
