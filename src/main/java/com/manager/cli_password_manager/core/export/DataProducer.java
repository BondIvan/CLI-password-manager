package com.manager.cli_password_manager.core.export;

import java.io.Writer;

@FunctionalInterface
public interface DataProducer {
    void writeData(Writer writer);
}
