package com.manager.cli_password_manager.core.entity.dto.export;

public record NoteExportDTO(
        String name,
        String login,
        String category,
        String password
) { }
