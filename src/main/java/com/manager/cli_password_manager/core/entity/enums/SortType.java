package com.manager.cli_password_manager.core.entity.enums;

import com.manager.cli_password_manager.core.exception.command.GetAllCommandException;

public enum SortType {
    NAME,
    CATEGORY;

    public static SortType fromString(String sortType) {
        for(SortType type: SortType.values()) {
            if(type.name().equalsIgnoreCase(sortType))
                return SortType.valueOf(sortType.toUpperCase());
        }

        throw new GetAllCommandException("Sorting by this value [" + sortType + "] doesnt support");
    }
}
