package com.manager.cli_password_manager.core.export;

import com.manager.cli_password_manager.core.entity.Note;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface NoteExporter {
    void export(Writer writer, Stream<Note> noteStream) throws IOException;
    ExportFormat getFormat();
}