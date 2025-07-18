package com.manager.cli_password_manager.core.repository;

import javax.crypto.SecretKey;
import java.util.Set;

public interface VaultRepository {
    void unlockVault(char[] password);
    void lockVault();
    boolean isVaultUnlocked();
    SecretKey getKey(String aliasId);
    void addKey(String aliasId, SecretKey key);
    void deleteKey(String aliasId);
    void updateKey(String aliasId, SecretKey key);
    Set<String> aliases(); //TODO Удалить
}
