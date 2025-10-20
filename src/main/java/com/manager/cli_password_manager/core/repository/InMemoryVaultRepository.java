package com.manager.cli_password_manager.core.repository;

import com.manager.cli_password_manager.core.exception.vault.VaultException;
import com.manager.cli_password_manager.core.service.file.loader.KeyStoreLoader;
import com.manager.cli_password_manager.core.service.vault.impl.VaultStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.crypto.SecretKey;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Slf4j
@Repository
public class InMemoryVaultRepository implements VaultRepository {
    private final KeyStoreLoader keyStoreLoader;
    private final VaultStateService vaultStateService;
    private KeyStore keyStoreInstance;

    public InMemoryVaultRepository(KeyStoreLoader keyStoreLoader, VaultStateService vaultStateService) {
        this.keyStoreLoader = keyStoreLoader;
        this.vaultStateService = vaultStateService;
    }

    @Override
    public void unlockVault(char[] password) {
        log.info("Unlocking the vault");
        keyStoreInstance = keyStoreLoader.loadKeyStore(password);
        vaultStateService.unlock(password);
    }

    @Override
    public void lockVault() {
        if (isVaultUnlocked()) {
            keyStoreInstance = null;
            vaultStateService.lock();
            log.info("Vault was locked");
            return;
        }

        log.info("Vault already locked");
    }

    @Override
    public boolean isVaultUnlocked() {
        return keyStoreInstance != null && vaultStateService.isUnlocked();
    }

    @Override
    public void updateKey(String aliasId, SecretKey newKey) {
        addKey(aliasId, newKey); // will be updated automatically inside
    }

    @Override
    public SecretKey getKey(String aliasId) {
        if (!isVaultUnlocked()) {
            log.error("Try to get key from vault, but vault is locked");
            throw new VaultException("Vault is locked");
        }

        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(vaultStateService.getVaultPassword());
        KeyStore.SecretKeyEntry secretKeyEntry;
        try {
            if (!keyStoreInstance.containsAlias(aliasId)) {
                log.error("There is no such id {} in keyStore for getting", aliasId);
                throw new VaultException("There is no such id " + aliasId + " in keyStore");
            }

            secretKeyEntry = (KeyStore.SecretKeyEntry) keyStoreInstance.getEntry(aliasId, protectionParameter);
        } catch (Exception e) {
            log.error("Key store get key error: {}", e.getMessage());
            throw new VaultException("Key store get key error: " + e.getMessage(), e);
        }

        return secretKeyEntry.getSecretKey();
    }

    @Override
    public void addKey(String aliasId, SecretKey key) {
        if (!isVaultUnlocked()) {
            log.error("Cannot save key to the vault, because vault is locked");
            throw new VaultException("Vault is locked");
        }

        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(key);
        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(vaultStateService.getVaultPassword());

        try {
            keyStoreInstance.setEntry(aliasId, secretKeyEntry, protectionParameter);
        } catch (KeyStoreException e) {
            log.error("Cannot set key to the keyStore: {}", e.getMessage());
            throw new VaultException("Cannot set key to the keyStore: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteKey(String aliasId) {
        if (!isVaultUnlocked()) {
            log.error("Cannot delete key from vault, because vault is locked");
            throw new VaultException("Vault is locked");
        }

        try {
            if (!keyStoreInstance.containsAlias(aliasId)) {
                log.info("There is no such id {} in keyStore for deleting", aliasId);
                throw new VaultException("There is no such id " + aliasId + " in keyStore");
            }

            keyStoreInstance.deleteEntry(aliasId);
        } catch (KeyStoreException e) {
            log.error("Cannot delete key from the keyStore: {}", e.getMessage());
            throw new VaultException("Cannot delete key from the keyStore: " + e.getMessage(), e);
        }
    }

    @Override
    public Set<String> aliases() {
        Set<String> set = new HashSet<>();

        try {
            Iterator<String> iterator = keyStoreInstance.aliases().asIterator();
            while(iterator.hasNext())
                set.add(iterator.next());
        } catch (Exception e) {
            throw new VaultException("Cannot access to vault and cannot to get all aliases");
        }

        return set;
    }

    public KeyStore getKeyStoreInstance() {
        return keyStoreInstance;
    }
}
