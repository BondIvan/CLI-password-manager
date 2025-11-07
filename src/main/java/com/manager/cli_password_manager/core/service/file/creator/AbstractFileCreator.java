package com.manager.cli_password_manager.core.service.file.creator;

import com.manager.cli_password_manager.core.exception.file.loader.FileCreatorException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Slf4j
public abstract class AbstractFileCreator {

    protected abstract void create(Path path) throws IOException;

    protected abstract Path createTmp(Path path, String prefix) throws IOException;

    public void createAndSecure(Path path) throws FileCreatorException {
        try {
            if(!Files.exists(path))
                create(path);

            secureFile(path);
        } catch (IOException e) {
            log.error("Cannot create file/directory by next path - [{}] by reason: {}", path, e.getMessage());
            throw new FileCreatorException("Creating file/directory error: " + e.getMessage(), e);
        }
    }

    public Path createTmpAndSecure(Path path, String prefix) throws FileCreatorException {
        try {
            Path securedPath = createTmp(path, prefix);

            secureFile(securedPath);

            return securedPath;
        } catch (IOException e) {
            log.error("Cannot create tmp file/directory by next path - [{}] by reason: {}", path, e.getCause().getMessage());
            throw new FileCreatorException("Creating tmp file/directory error: " + e.getMessage(), e);
        }
    }

    private void secureFile(Path path) throws IOException {
        FileStore fileStore = Files.getFileStore(path);

        boolean isWindows = fileStore.supportsFileAttributeView(AclFileAttributeView.class);
        boolean isLinuxOrMac = fileStore.supportsFileAttributeView(PosixFileAttributeView.class);

        if(isWindows) {
            try {
                setAclPermissions(path);
            } catch (IOException e) {
                log.warn("Не удалось применить права ACL: {}", e.getMessage());
                throw new FileCreatorException("Не удалось применить права ACL: " + e.getMessage(), e);
            }
        }

        if(isLinuxOrMac) {
            try {
                setPosixPermissions(path);
            } catch (IOException e) {
                log.warn("Не удалось применить права POSIX: {}", e.getMessage());
                throw new FileCreatorException("Не удалось применить права POSIX: " + e.getMessage(), e);
            }
        }
    }

    private void setAclPermissions(Path filePath) throws IOException {
        AclFileAttributeView aclView = Files.getFileAttributeView(filePath, AclFileAttributeView.class);
        UserPrincipal owner = Files.getOwner(filePath);

        EnumSet<AclEntryPermission> fullAccessPermissions = EnumSet.of(
                AclEntryPermission.READ_DATA,
                AclEntryPermission.WRITE_DATA,
                AclEntryPermission.APPEND_DATA,
                AclEntryPermission.READ_NAMED_ATTRS,
                AclEntryPermission.WRITE_NAMED_ATTRS,
                AclEntryPermission.EXECUTE,
                AclEntryPermission.DELETE,
                AclEntryPermission.DELETE_CHILD,
                AclEntryPermission.READ_ATTRIBUTES,
                AclEntryPermission.WRITE_ATTRIBUTES,
                AclEntryPermission.READ_ACL,
                AclEntryPermission.WRITE_ACL,
                AclEntryPermission.WRITE_OWNER,
                AclEntryPermission.SYNCHRONIZE
        );

        // Create ACL record that gives full access only to the owner
        AclEntry aclEntry = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(owner)
                .setPermissions(fullAccessPermissions)
                .build();

        // Replace all existing rules with one - our
        aclView.setAcl(Collections.singletonList(aclEntry));
    }

    private void setPosixPermissions(Path filePath) throws IOException {
        Set<PosixFilePermission> permissions = EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE
        );

        Files.setPosixFilePermissions(filePath, permissions);
    }
}
