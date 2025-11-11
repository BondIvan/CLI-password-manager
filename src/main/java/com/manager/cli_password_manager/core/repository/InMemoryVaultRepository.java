package com.manager.cli_password_manager.core.repository;

import com.manager.cli_password_manager.core.exception.vault.VaultException;
import com.manager.cli_password_manager.core.service.file.creator.SecureFileCreator;
import com.manager.cli_password_manager.core.service.file.loader.KeyStoreLoader;
import com.manager.cli_password_manager.core.service.annotation.filetransaction.FileTransactionManager;
import com.manager.cli_password_manager.core.service.vault.impl.VaultStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository("memoryVaultRepository")
@RequiredArgsConstructor
public class InMemoryVaultRepository implements VaultRepository, FileTransactionCommitRollback {
    private final KeyStoreLoader keyStoreLoader;
    private final VaultStateService vaultStateService;
    private final FileTransactionManager fileTransactionManager;
    private final SecureFileCreator fileCreator;

    private KeyStore keyStoreInstance;

    @Override
    public void saveToFile() {
        try {
            Optional<Path> appTmpSavingDir = fileTransactionManager.getCurrentTransactionalDirectory();
            if (appTmpSavingDir.isPresent()) {
                Path tmpSavingDir = appTmpSavingDir.get();
                Path tmpKeyStoreFilePath = fileCreator.createTmpAndSecure(tmpSavingDir, "ks-");
                Path originalFilePath = keyStoreLoader.getVaultPathFile();

                keyStoreLoader.saveKeyStoreToFile(keyStoreInstance, vaultStateService.getVaultPassword(), tmpKeyStoreFilePath);

                fileTransactionManager.registerFile(originalFilePath, tmpKeyStoreFilePath);

                log.info("Tmp vault file created successfully");
            } else {
                throw new RuntimeException("Метод должен быть транзакционным - [FileTransaction]");
            }
        } catch (IOException e) {
            throw new VaultException("Error saving file: cannot create tmp keyStore file", e);
        }
    }

    @Override
    public void rollbackFileState() {
        log.warn("Rolling back in memory state for vault repository");
        unlockVault(vaultStateService.getVaultPassword());
    }

    @Override
    public void unlockVault(char[] password) {
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
        fileTransactionManager.registerRepoParticipant(this);
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
                log.warn("There is no such id {} in keyStore for getting", aliasId);
                throw new VaultException("There is no such id " + aliasId + " in keyStore");
            }

            secretKeyEntry = (KeyStore.SecretKeyEntry) keyStoreInstance.getEntry(aliasId, protectionParameter);
        } catch (Exception e) {
            log.error("Key store get key error", e);
            throw new VaultException("Key store get key error: " + e.getMessage(), e);
        }

        return secretKeyEntry.getSecretKey();
    }

    @Override
    public void addKey(String aliasId, SecretKey key) {
        fileTransactionManager.registerRepoParticipant(this);

        if (!isVaultUnlocked()) {
            log.error("Cannot save key to the vault, because vault is locked");
            throw new VaultException("Vault is locked");
        }

        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(key);
        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(vaultStateService.getVaultPassword());

        try {
            keyStoreInstance.setEntry(aliasId, secretKeyEntry, protectionParameter);
        } catch (KeyStoreException e) {
            log.error("Cannot set key to the keyStore", e);
            throw new VaultException("Cannot set key to the keyStore: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteKey(String aliasId) {
        fileTransactionManager.registerRepoParticipant(this);

        if (!isVaultUnlocked()) {
            log.error("Cannot delete key from vault, because vault is locked");
            throw new VaultException("Vault is locked");
        }

        try {
            if (!keyStoreInstance.containsAlias(aliasId)) {
                log.warn("There is no such id {} in keyStore for deleting", aliasId);
                throw new VaultException("There is no such id " + aliasId + " in keyStore");
            }

            keyStoreInstance.deleteEntry(aliasId);
        } catch (KeyStoreException e) {
            log.error("Cannot delete key from the keyStore", e);
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

    public KeyStore getKeyStoreInstance() { //TODO Скорее всего не понадобится
        return keyStoreInstance;
    }
}
