package com.manager.cli_password_manager.core.service.annotation;

import com.manager.cli_password_manager.core.exception.file.loader.FileCreatorException;
import com.manager.cli_password_manager.core.exception.file.loader.FileTransactionAspectException;
import com.manager.cli_password_manager.core.repository.FileTransactionCommitRollback;
import com.manager.cli_password_manager.core.service.Pair;
import com.manager.cli_password_manager.core.service.file.creator.SecureDirectoryCreator;
import com.manager.cli_password_manager.core.service.file.creator.directory.ApplicationDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FileTransactionAspect {
    private final ApplicationDirectory applicationDirectory;
    private final SecureDirectoryCreator directoryCreator;
    private final FileTransactionManager fileTransactionManager;

    @Around("@annotation(fileTransaction)")
    public Object manageFileTransactionSaving(ProceedingJoinPoint joinPoint, FileTransaction fileTransaction) throws Throwable {
        if(fileTransactionManager.isTransactionActive()) {
            log.info("Transaction already exists. Joining to ongoing file transaction");
            return joinPoint.proceed(); // just do it
        }

        log.info("Start file transaction with name - {}", fileTransaction.name());

        Path tmpDirectory = createTmpDirectory();
        fileTransactionManager.startTransaction(tmpDirectory);

        try {
            Object result = joinPoint.proceed();

            log.info("Saving transaction [{}] finished successfully. Commiting...", fileTransaction.name());

            commit();

            return result;
        } catch (Throwable e) {
            log.warn("Some exception during saving transaction [{}]", fileTransaction.name());

            // decision about rollback
            if(shouldRollback(e, fileTransaction)) {
                log.warn("Rollback required. Start rollback...");
                rollback(tmpDirectory, fileTransaction); // delete tmp files and do not replace original files
            } else {
                log.warn("Rollback not needed. Cleaning up tmp files");
            }

            throw e;
        } finally {
            log.info("Cleaning up temp files for transaction [{}]...", fileTransaction.name());
            FileSystemUtils.deleteRecursively(tmpDirectory);

            fileTransactionManager.endTransaction();
            log.info("End saving transaction [{}]", fileTransaction.name());
        }
    }

    private Path createTmpDirectory() throws FileCreatorException {
        Path appDir = applicationDirectory.getApplicationDirectory();
        Path tmpTransactionSavingDir = directoryCreator.createTmpAndSecure(appDir, "tmpDir-");

        log.info("Tmp directory was created");

        return tmpTransactionSavingDir;
    }

    private void commit() throws IOException {
        log.info("Start making commit...");

        // save tmp files
        Set<FileTransactionCommitRollback> savingRepo = fileTransactionManager.getRepoParticipants();
        savingRepo.forEach(FileTransactionCommitRollback::saveToFile);

        List<Pair<Path, Path>> commitingFiles = fileTransactionManager.getPendingFiles();
        if(commitingFiles.isEmpty()) {
            log.warn("There are no files for transaction commit");
            return;
        }

        Optional<Path> notExistTmpFile = commitingFiles.stream()
                .map(Pair::tmpFilePath)
                .filter(path -> !Files.exists(path))
                .findFirst();

        if(notExistTmpFile.isPresent())
            throw new IOException("Commit aborted: tmp file [" + notExistTmpFile.get() + "] not exist");

        for(Pair<Path, Path> pairFile: commitingFiles) {
            Files.move(
                    pairFile.tmpFilePath(), pairFile.originalFilePath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

            log.info("File is committed: tmp:{} -> origin:{}",
                    pairFile.tmpFilePath().getFileName(), pairFile.originalFilePath().getFileName());
        }

        log.info("The commit completed successfully");
    }

    private void rollback(Path tmpDirectory, FileTransaction fileTransaction) {
        log.warn("Rolling back transaction - {} ...", tmpDirectory);

        Set<FileTransactionCommitRollback> repoParticipants = fileTransactionManager.getRepoParticipants();

        for(FileTransactionCommitRollback rollbackLoading: repoParticipants) {
            try {
                rollbackLoading.rollbackFileState();
            } catch (Exception e) {
                switch (fileTransaction.rollbackFailureAction()) {
                    case RollbackFailureAction.THROW_EXCEPTION ->
                            throw new FileTransactionAspectException(String.format("Error: Failed to rollback state for participant: %s. By reason: %s",
                                    rollbackLoading.getClass().getSimpleName(), e));
                    case RollbackFailureAction.LOG_WARNING ->
                            log.error("Error: Failed to rollback state for participant: {}. By reason: {}",
                                    rollbackLoading.getClass().getSimpleName(), e.getMessage());
                }
            }
        }
    }

    private boolean shouldRollback(Throwable throwable, FileTransaction fileTransaction) {
        // no rollback needed
        for(Class<? extends Throwable> noRollbackExceptionClass: fileTransaction.noRollbackFor()) {
            if(noRollbackExceptionClass.isInstance(throwable))
                return false;
        }

        // rollback needed
        if(fileTransaction.rollbackFor().length > 0) {
            for(Class<? extends Throwable> rollbackExceptionClass: fileTransaction.rollbackFor()) {
                if(rollbackExceptionClass.isInstance(throwable))
                    return true;
            }
            return false;
        }

        return (throwable instanceof RuntimeException || throwable instanceof Error);
    }
}
