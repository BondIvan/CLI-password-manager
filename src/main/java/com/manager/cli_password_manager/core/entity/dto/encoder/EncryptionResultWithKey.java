package com.manager.cli_password_manager.core.entity.dto.encoder;

import javax.crypto.SecretKey;

public record EncryptionResultWithKey (
        String encryptedPassword,
        SecretKey key
) {
}
