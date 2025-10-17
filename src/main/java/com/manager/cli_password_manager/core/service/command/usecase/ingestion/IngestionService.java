package com.manager.cli_password_manager.core.service.command.usecase.ingestion;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.converter.StringIngestionFormatConverter;
import com.manager.cli_password_manager.core.entity.enums.IngestionFormat;
import com.manager.cli_password_manager.core.exception.IO.IngestionException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class IngestionService {
    private final Map<IngestionFormat, NoteIngester> importers;
    private final StringIngestionFormatConverter importFormatConverter;

    public IngestionService(List<NoteIngester> importerList,
                            StringIngestionFormatConverter importFormatConverter) {
        this.importers = importerList.stream()
                .collect(Collectors.toMap(
                        NoteIngester::getFormat,
                        Function.identity()
                ));

        this.importFormatConverter = importFormatConverter;
    }

    public IngestionFormat getImportFormatFromFileExtension(Path path) {
        String fileName = path.getFileName().toString();
        int indexOfDot = fileName.lastIndexOf(".");
        if(indexOfDot < 0)
            throw new IngestionException("Unknown file extension");

        String extension = fileName.substring(indexOfDot + 1);
        return importFormatConverter.toImportFormat(extension);
    }

    public Map<String, List<Note>> importFromFile(IngestionFormat format, IngestionContext context) throws IOException {
        NoteIngester importer = importers.get(format);
        if(importer == null)
            throw new IngestionException("Unsupported import format: " + format);

        try(InputStream inputStream = Files.newInputStream(context.filePath())) {
            return importer.importNotes(context, inputStream);
        }
    }
}
