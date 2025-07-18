package com.manager.cli_password_manager.core.entity.dto.command;

import java.util.Objects;

public record NoteNamePlusLoginDTO(
    String name,
    String login
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteNamePlusLoginDTO that = (NoteNamePlusLoginDTO) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
