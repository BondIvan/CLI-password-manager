package com.manager.cli_password_manager.core.entity.dto.command;

import com.manager.cli_password_manager.core.entity.enums.Category;

public record DecryptedNoteDTO(
    String name,
    String login,
    String password,
    Category category
) {
    @Override
    public String toString() {
        return String.format("Name: %s\nCategory: %s\nLogin: %s\nPassword: %s",
                name, category, login, "*****");
    }
}
