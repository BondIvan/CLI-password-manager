package com.manager.cli_password_manager.core.service.command.usecase.export;

import com.manager.cli_password_manager.core.exception.IO.export.ExportException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExportService {
    private final Map<ExportFormat, NoteExporter> exporters;

    public void exportToFile(Path path, ExportFormat format, ExportContext context) throws IOException {
        NoteExporter exporter = exporters.get(format);
        if(exporter == null)
            throw new ExportException("Unsupported export format: " + format);

        try(OutputStream fileOutputStream = new BufferedOutputStream(Files.newOutputStream(path))) {
            exporter.export(context, fileOutputStream);
        }
    }
}
