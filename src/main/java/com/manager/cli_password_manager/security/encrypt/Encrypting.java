package com.manager.cli_password_manager.security.encrypt;

import com.manager.cli_password_manager.core.entity.dto.encoder.EncryptionResultWithKey;

import javax.crypto.SecretKey;

public interface Encrypting {
    EncryptionResultWithKey encryptPassword(String data);
    byte[] encryptMasterPassword(String data, byte[] secretKey, byte[] IV);
    String decryptPassword(SecretKey key, String data);
}
