package com.manager.cli_password_manager.core.export;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExporterFactory {
    private final Map<ExportFormat, NoteExporter> exporters;

    public ExporterFactory(List<NoteExporter> noteExporters) {
        this.exporters = noteExporters.stream()
                .collect(Collectors.toMap(
                        NoteExporter::getFormat,
                        Function.identity(),
                        (e1, e2) -> e1,
                        () -> new EnumMap<>(ExportFormat.class)
                ));
    }

    public Optional<NoteExporter> getExporter(ExportFormat format) {
        return Optional.ofNullable(exporters.get(format));
    }
}
