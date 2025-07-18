package com.manager.cli_password_manager.core.entity.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum Category {
    FINANCE("finance"),
    GAME("game"),
    MAIL("mail"),
    SHOP("shop"),
    EDUCATION("education"),
    SOCIAL("social"),
    ENTERTAINMENT("entertainment"),
    CORPORATE("corporate"),
    MULTIMEDIA("multimedia"),
    OTHER("other"),
    NO_CATEGORY("no_category");

    private final String title;

    Category(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static Set<String> titleNames() {
        return Arrays.stream(values())
                .map(Category::getTitle)
                .collect(Collectors.toSet());
    }
}
