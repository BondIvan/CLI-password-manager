package com.manager.cli_password_manager.core.entity.dto.checker;

import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;

import java.util.Optional;

public record CheckerResult(
    NoteNamePlusLoginDTO namePlusLoginDTO,
    Boolean isPwned,
    Optional<String> message
) { }
