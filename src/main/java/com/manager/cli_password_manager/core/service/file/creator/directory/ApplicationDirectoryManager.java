package com.manager.cli_password_manager.core.service.file.creator.directory;

import java.nio.file.Path;

public interface ApplicationDirectoryManager {
    Path getApplicationDirectory();
    boolean isApplicationDirectoryExist();
    void create();
}
