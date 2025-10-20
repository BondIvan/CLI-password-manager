package com.manager.cli_password_manager.core.entity.dto.io;

public record EncryptedExportContainer(
        String version,
        String algorithm,
        String kdfAlgorithm,
        String saltBase64View,
        String ivBase64View,
        String cipherText
) { }
