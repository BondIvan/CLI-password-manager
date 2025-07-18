package com.manager.cli_password_manager.core.entity.converter;

import com.manager.cli_password_manager.core.entity.enums.ReplaceType;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StringReplaceTypeConverter {
    public ReplaceType toReplaceType(String str) {
        if(str == null)
            throw new IllegalArgumentException("Replace type cannot be null");

        String lowerCaseStrType = str.toLowerCase();
        for(ReplaceType type: ReplaceType.values()) {
            if(type.getTitle().equals(lowerCaseStrType))
                return type;
        }

        throw new IllegalArgumentException("Invalid replacement type - [" + str + "]");
    }

    public String toUpperCaseString(ReplaceType type) {
        Objects.requireNonNull(type);
        return type.name();
    }

    public String toLowerCaseString(ReplaceType type) {
        Objects.requireNonNull(type);
        return type.name().toLowerCase();
    }
}
