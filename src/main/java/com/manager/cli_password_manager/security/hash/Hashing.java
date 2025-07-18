package com.manager.cli_password_manager.security.hash;

public interface Hashing {
    byte[] hashWithSalt(String data, byte[] salt);
    byte[] generateSalt();
}
