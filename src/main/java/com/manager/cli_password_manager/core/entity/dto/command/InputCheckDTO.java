package com.manager.cli_password_manager.core.entity.dto.command;

import com.manager.cli_password_manager.core.entity.enums.CheckingApi;

import java.util.Optional;

public record InputCheckDTO(
        CheckingApi api,
        Optional<String> serviceName,
        Optional<String> login
) { }
