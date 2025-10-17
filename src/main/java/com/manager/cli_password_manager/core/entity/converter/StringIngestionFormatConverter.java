package com.manager.cli_password_manager.core.entity.converter;

import com.manager.cli_password_manager.core.entity.enums.IngestionFormat;
import org.springframework.stereotype.Component;

@Component
public class StringIngestionFormatConverter {
    public IngestionFormat toImportFormat(String str) {
        if(str == null || str.isEmpty())
            throw new IllegalArgumentException("Import format cannot be null or empty");

        String lowercaseStr = str.toLowerCase();
        for(IngestionFormat format: IngestionFormat.values()) {
            if(format.name().toLowerCase().equals(lowercaseStr))
                return format;
        }

        throw new IllegalArgumentException("Invalid import format - [" + str + "]");
    }
}
