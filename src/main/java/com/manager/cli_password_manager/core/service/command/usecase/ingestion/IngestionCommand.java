package com.manager.cli_password_manager.core.service.command.usecase.ingestion;

import com.manager.cli_password_manager.core.entity.enums.IngestionResult;
import com.manager.cli_password_manager.core.service.command.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionCommand {
    private final NoteService noteService;

    public IngestionResult execute(String filePath, String password) {
        return noteService.ingestion(filePath, password);
    }
}
