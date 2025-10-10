package com.manager.cli_password_manager.security.encrypt;

public interface PasswordEncryptor {
    String encryptPassword(String noteId, String servicePassword);
    String decryptPassword(String noteId, String servicePassword);
}
