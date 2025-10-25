package com.manager.cli_password_manager.core.service.annotation;

import com.manager.cli_password_manager.core.exception.file.loader.TransactionManagerException;
import com.manager.cli_password_manager.core.repository.FileTransactionRollbackLoading;
import com.manager.cli_password_manager.core.service.Pair;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class FileTransactionManager {
    private static final ThreadLocal<FileTransactionContext> currentTransactionContext = new ThreadLocal<>();

    public boolean isTransactionActive() {
        return currentTransactionContext.get() != null;
    }

    public void startTransaction(Path tmpDir) {
        currentTransactionContext.set(new FileTransactionContext(tmpDir));
    }

    public void endTransaction() {
        currentTransactionContext.remove();
    }

    public Optional<Path> getCurrentTransactionalDirectory() {
        return getCurrentContext().map(FileTransactionContext::getTmpSavingDirectory);
    }

    public void registerFile(Path originalPath, Path tmpPath) {
        FileTransactionContext ftc = getCurrentContext().orElseThrow(
                () -> new TransactionManagerException("Repo cannot register file: no active transaction")
        );

        ftc.addFile(originalPath, tmpPath);
    }

    public void registerRepoParticipant(FileTransactionRollbackLoading participant) {
        FileTransactionContext ftc = getCurrentContext().orElseThrow(
                () -> new TransactionManagerException("Repo cannot be the participant for rollback state: no active transaction")
        );

        ftc.addRepoParticipant(participant);
    }

    public List<Pair<Path, Path>> getPendingFiles() {
        return getCurrentContext()
                .map(FileTransactionContext::getPendingFiles)
                .orElse(Collections.emptyList());
    }

    public Set<FileTransactionRollbackLoading> getRepoParticipants() {
        return getCurrentContext()
                .map(FileTransactionContext::getRepoParticipant)
                .orElse(Collections.emptySet());
    }

    private Optional<FileTransactionContext> getCurrentContext() {
        return Optional.ofNullable(currentTransactionContext.get());
    }
}
