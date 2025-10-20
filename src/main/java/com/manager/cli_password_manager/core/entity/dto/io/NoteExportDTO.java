package com.manager.cli_password_manager.core.entity.dto.io;

public record NoteExportDTO(
        String name,
        String login,
        String category,
        String password
) { }
