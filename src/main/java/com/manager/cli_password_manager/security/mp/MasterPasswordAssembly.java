package com.manager.cli_password_manager.security.mp;

public interface MasterPasswordAssembly {
    byte[] encodeMasterPassword(String masterPassword);
    byte[] decodeMasterPassword(byte[] masterPasswordBytes, String inputPassword);
}
