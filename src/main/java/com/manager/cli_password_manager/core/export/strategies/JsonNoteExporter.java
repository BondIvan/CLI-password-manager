package com.manager.cli_password_manager.core.export.strategies;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.export.EncryptedExportContainer;
import com.manager.cli_password_manager.core.entity.dto.export.NoteExportDTO;
import com.manager.cli_password_manager.core.entity.enums.ExportEncryptorAlgorithm;
import com.manager.cli_password_manager.core.entity.mapper.NoteMapper;
import com.manager.cli_password_manager.core.export.ExportContext;
import com.manager.cli_password_manager.core.export.ExportFormat;
import com.manager.cli_password_manager.core.export.NoteExporter;
import com.manager.cli_password_manager.core.service.export.ExportEncryptionService;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Component
public class JsonNoteExporter implements NoteExporter {
    private final ObjectMapper objectMapper;
    private final ExportEncryptionService exportEncryptorService;
    private final NoteMapper noteMapper;

    public JsonNoteExporter(ObjectMapper objectMapper,
                            ExportEncryptionService exportEncryptorService,
                            NoteMapper noteMapper) {
        this.objectMapper = objectMapper.copy();
        this.exportEncryptorService = exportEncryptorService;
        this.noteMapper = noteMapper;
    }

    @Override
    public void export(ExportContext context, OutputStream outputStream) throws IOException {
        Map<String, List<Note>> exportedData = context.notes();
        if(context.password() != null)
            encryptedExport(exportedData, context.password(), outputStream);
        else
            plainExport(exportedData, outputStream);
    }

    private void encryptedExport(Map<String, List<Note>> data, char[] password, OutputStream outputStream) throws IOException {
        byte[] plainTextBytes;

        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            plainExport(data, baos);
            plainTextBytes = baos.toByteArray();
        }

        //TODO ExportEncryptorAlgorithm нужно заменить на какую-то абстракцию (в будущем конфиг файл)
        EncryptedExportContainer exportContainer = exportEncryptorService
                .exportData(plainTextBytes, ExportEncryptorAlgorithm.AES_GCM_256, password);

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, exportContainer);
    }

    private void plainExport(Map<String, List<Note>> data, OutputStream outputStream) throws IOException {
        try(JsonGenerator generator = objectMapper.getFactory().createGenerator(outputStream)) {
            generator.setPrettyPrinter(new DefaultPrettyPrinter());
            generator.writeStartObject();

            for(Map.Entry<String, List<Note>> entry: data.entrySet()) {
                generator.writeFieldName(entry.getKey());
                generator.writeStartArray();

                for(Note note: entry.getValue()) {
                    NoteExportDTO noteExportDTO = noteMapper.toExportDTO(note);
                    generator.writeObject(noteExportDTO);
                }

                generator.writeEndArray();
            }

            generator.writeEndObject();
        }
    }

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.JSON;
    }
}
