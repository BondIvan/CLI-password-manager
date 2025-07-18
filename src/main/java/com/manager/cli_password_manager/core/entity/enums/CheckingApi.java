package com.manager.cli_password_manager.core.entity.enums;

public enum CheckingApi {
    HIBP("hibp");

    private final String name;

    CheckingApi(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
