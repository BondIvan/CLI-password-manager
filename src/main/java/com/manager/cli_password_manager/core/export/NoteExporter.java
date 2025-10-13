package com.manager.cli_password_manager.core.export;

import java.io.IOException;
import java.io.OutputStream;

public interface NoteExporter {
    void export(ExportContext context, OutputStream outputStream) throws IOException;
    ExportFormat getFormat();
}