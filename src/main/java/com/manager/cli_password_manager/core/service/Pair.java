package com.manager.cli_password_manager.core.service;

public record Pair<K, V>(
        K originalFilePath,
        V tmpFilePath
) { }
