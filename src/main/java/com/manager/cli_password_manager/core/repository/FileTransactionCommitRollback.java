package com.manager.cli_password_manager.core.repository;

/**
 * Must implement repositories that work with files to rollback and save the state
 */

public interface FileTransactionCommitRollback {
    /**
     * Revert to original state from file. Use with any methods that modify in memory state
     */
    void rollbackFileState();

    /**
     * Save tmp file to the storage
     */
    void saveToFile();
}
