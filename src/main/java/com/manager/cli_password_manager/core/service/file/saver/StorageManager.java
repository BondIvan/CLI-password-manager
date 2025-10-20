package com.manager.cli_password_manager.core.service.file.saver;

import com.manager.cli_password_manager.core.exception.repository.StorageManagerException;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import com.manager.cli_password_manager.core.service.file.loader.AccessLoader;
import com.manager.cli_password_manager.core.service.file.loader.KeyStoreLoader;
import com.manager.cli_password_manager.core.repository.InMemoryVaultRepository;
import com.manager.cli_password_manager.core.service.vault.impl.VaultStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageManager {
    private final KeyStoreLoader keyStoreLoader;
    private final AccessLoader accessLoader;
    private final VaultStateService vaultStateService;
    private final InMemoryVaultRepository vaultRepository;
    private final InMemoryNotesRepository notesRepository;

    public void transactionalFilesSave() {
        Path vaultPath = keyStoreLoader.getVaultPathFile();
        Path accessPath = accessLoader.getAccessFilePath();

        Path vaultTmpPath = vaultPath.resolveSibling(vaultPath.getFileName().toString() + ".tmp");
        Path accessTmpPath = accessPath.resolveSibling(accessPath.getFileName().toString() + ".tmp");

        try {
            keyStoreLoader.saveKeyStoreToFile(vaultRepository.getKeyStoreInstance(), vaultStateService.getVaultPassword(), vaultTmpPath);
            accessLoader.saveAccessToFile(notesRepository.getAllNotes(), accessTmpPath);

            Files.move(vaultTmpPath, vaultPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            Files.move(accessTmpPath, accessPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

            log.info("Files was successfully saved to the disk");
        } catch (Exception e) {
            // Rollback
            try {
                Files.deleteIfExists(vaultTmpPath);
                Files.deleteIfExists(accessTmpPath);
            } catch (IOException tmpFileEx) {
                e.addSuppressed(tmpFileEx);
            }

            log.error("Rollback: new data was not added by reason: {}", e.getMessage());
            throw new StorageManagerException("Не удалось атомарно сохранить состояние хранилища. Изменения отменены.", e);
        }
    }
}
