package com.manager.cli_password_manager.core.export.strategies;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.export.ExportFormat;
import com.manager.cli_password_manager.core.export.NoteExporter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class JsonNoteExporter implements NoteExporter {
    private final ObjectMapper objectMapper;

    public JsonNoteExporter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void export(Writer writer, Stream<Note> noteStream) throws IOException {
        objectMapper.writeValue(writer, noteStream);
    }

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.JSON;
    }
}
