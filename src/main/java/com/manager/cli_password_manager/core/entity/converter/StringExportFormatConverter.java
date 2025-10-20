package com.manager.cli_password_manager.core.entity.converter;

import com.manager.cli_password_manager.core.service.command.usecase.export.ExportFormat;
import org.springframework.stereotype.Component;

@Component
public class StringExportFormatConverter {
    public ExportFormat toExportFormat(String str) {
        if(str == null || str.isEmpty())
            throw new IllegalArgumentException("Export format cannot be null or empty");

        String lowercaseStr = str.toLowerCase();
        for(ExportFormat format: ExportFormat.values()) {
            if(format.name().toLowerCase().equals(lowercaseStr))
                return format;
        }

        throw new IllegalArgumentException("Invalid export format - [" + str + "]");
    }
}
