package com.manager.cli_password_manager.core.entity.dto.command;

import com.manager.cli_password_manager.core.entity.enums.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InputAddDTO(
        @NotNull @NotBlank String serviceName,
        @NotNull @NotBlank String login,
        String password, //TODO Create and add @Password
        Category category,
        boolean isAutoGeneratePassword,
        char[] excludedSymbols
) { }
