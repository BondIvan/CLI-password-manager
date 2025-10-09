package com.manager.cli_password_manager.core.export;

import java.io.IOException;
import java.nio.file.Path;

public interface ExportProtectApproach {
    void export(Path exportPath, DataProducer producer, char[] password) throws IOException;
}
