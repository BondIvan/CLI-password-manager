package com.manager.cli_password_manager.core.entity.dto.encoder;

import javax.crypto.SecretKey;

public record EncryptionResult(
    String base64EncryptedResult,
    SecretKey key,
    byte[] salt,
    byte[] iv
) { }
