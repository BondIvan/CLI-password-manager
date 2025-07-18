package com.manager.cli_password_manager.core.entity.dto.command;

import com.manager.cli_password_manager.core.entity.enums.ReplaceType;

public record InputReplaceDTO(
    String serviceName,
    String login,
    ReplaceType type,
    String value
) { }
