package com.manager.cli_password_manager.core.service.annotation;

import com.manager.cli_password_manager.core.repository.FileTransactionRollbackLoading;
import com.manager.cli_password_manager.core.service.Pair;
import lombok.Getter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contains data about current saving transaction
 */

@Getter
public class FileTransactionContext {
    private final Path tmpSavingDirectory;
    private final List<Pair<Path, Path>> pendingFiles;
    private final Set<FileTransactionRollbackLoading> repoParticipant;

    public FileTransactionContext(Path tmpSavingDirectory) {
        this.tmpSavingDirectory = tmpSavingDirectory;
        this.pendingFiles = new ArrayList<>();
        this.repoParticipant = new HashSet<>();
    }

    public void addFile(Path originalFilePath, Path tmpFilePath) {
        pendingFiles.add(
                new Pair<>(originalFilePath, tmpFilePath)
        );
    }

    public void addRepoParticipant(FileTransactionRollbackLoading participant) {
        repoParticipant.add(participant);
    }
}
