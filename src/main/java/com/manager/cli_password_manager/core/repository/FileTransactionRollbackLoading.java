package com.manager.cli_password_manager.core.repository;

/**
 * Must implement repositories that work with files to rollback the state
 */

public interface FileTransactionRollbackLoading {
    /**
     * Revert to original state from file. Use with any methods that modify in memory state
     */
    void rollbackFileState();
}
