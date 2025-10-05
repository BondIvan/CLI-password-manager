package com.manager.cli_password_manager.core.export.strategies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.exception.IO.NoteExportException;
import com.manager.cli_password_manager.core.export.ExportFormat;
import com.manager.cli_password_manager.core.export.NoteExporter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class JsonExporter implements NoteExporter {
    private final ObjectMapper objectMapper;

    public JsonExporter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String export(Map<String, List<Note>> notes) {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return objectMapper.writeValueAsString(notes);
        } catch (JsonProcessingException e) {
            throw new NoteExportException("JSON export error: " + e.getMessage(), e);
        }
    }

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.JSON;
    }
}
