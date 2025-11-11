package com.manager.cli_password_manager.core.service.command.usecase.ingestion;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.converter.StringIngestionFormatConverter;
import com.manager.cli_password_manager.core.entity.enums.IngestionFormat;
import com.manager.cli_password_manager.core.exception.IO.ingestion.IngestionException;
import com.manager.cli_password_manager.core.exception.IO.ingestion.IngestionFileProtectedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IngestionService {
    private final Map<IngestionFormat, NoteIngester> importers;
    private final StringIngestionFormatConverter importFormatConverter;

    public IngestionFormat getImportFormatFromFileExtension(Path path) {
        String fileName = path.getFileName().toString();
        int indexOfDot = fileName.lastIndexOf(".");
        if(indexOfDot < 0)
            throw new IngestionException("Unknown file extension");

        String extension = fileName.substring(indexOfDot + 1);
        try {
            return importFormatConverter.toImportFormat(extension);
        } catch (IllegalArgumentException e) {
            throw new IngestionException("Unknown import format", e);
        }
    }

    public Map<String, List<Note>> importFromFile(IngestionFormat format, IngestionContext context) {
        NoteIngester importer = importers.get(format);
        if(importer == null)
            throw new IngestionException("Unsupported import format: " + format);

        try(InputStream inputStream = Files.newInputStream(context.filePath())) {
            return importer.importNotes(context, inputStream);
        } catch (IngestionFileProtectedException e) {
            throw new IngestionException(e);
        } catch (IOException e) {
            throw new IngestionException("Cannot find the file", e);
        }
    }
}
