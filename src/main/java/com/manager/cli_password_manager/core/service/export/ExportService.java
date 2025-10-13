package com.manager.cli_password_manager.core.service.export;

import com.manager.cli_password_manager.core.exception.export.ExportException;
import com.manager.cli_password_manager.core.export.ExportContext;
import com.manager.cli_password_manager.core.export.ExportFormat;
import com.manager.cli_password_manager.core.export.NoteExporter;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExportService {
    private final Map<ExportFormat, NoteExporter> exporters;

    public ExportService(List<NoteExporter> exporterList) {
        this.exporters = exporterList.stream()
                .collect(Collectors.toMap(
                        NoteExporter::getFormat,
                        Function.identity()
                ));
    }

    public void exportToFile(Path path, ExportFormat format, ExportContext context) throws IOException {
        NoteExporter exporter = exporters.get(format);
        if(exporter == null)
            throw new ExportException("Unsupported export format: " + format);

        try(OutputStream fileOutputStream = new BufferedOutputStream(Files.newOutputStream(path))) {
            exporter.export(context, fileOutputStream);
        }
    }
}
