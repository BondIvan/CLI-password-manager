package com.manager.cli_password_manager.core.service.command.usecase.ingestion;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.enums.IngestionFormat;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface NoteIngester {
    Map<String, List<Note>> importNotes(IngestionContext context, InputStream inputStream);
    IngestionFormat getFormat();
}
