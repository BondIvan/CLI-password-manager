package com.manager.cli_password_manager.core.entity.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum ReplaceType {
    NAME("name"),
    LOGIN("login"),
    CATEGORY("category"),
    PASSWORD("password");

    private final String title;

    ReplaceType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static Set<String> titleNames() {
        return Arrays.stream(values())
                .map(ReplaceType::getTitle)
                .collect(Collectors.toSet());
    }
}
