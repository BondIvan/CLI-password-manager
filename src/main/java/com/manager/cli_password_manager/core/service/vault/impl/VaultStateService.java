package com.manager.cli_password_manager.core.service.vault.impl;

import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Этот сервис будет временно хранить мастер-пароль после успешного входа.
 */
@Component
public class VaultStateService {
    private char[] currentMasterPassword;
    private boolean isUnlocked = false;

    public void unlock(char[] password) {
        this.currentMasterPassword = Arrays.copyOf(password, password.length);
        this.isUnlocked = true;
    }

    public void lock() {
        if(currentMasterPassword != null) {
            Arrays.fill(currentMasterPassword, '\0');
            this.currentMasterPassword = null;
        }
        this.isUnlocked = false;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public char[] getVaultPassword() {
        if(!isUnlocked || currentMasterPassword == null)
            return null;

        return Arrays.copyOf(currentMasterPassword, currentMasterPassword.length);
    }
}
