package com.manager.cli_password_manager.core.export;

import com.manager.cli_password_manager.core.entity.Note;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface NoteExporter {
    String export(Map<String, List<Note>> notes);
    ExportFormat getFormat();
}