package com.manager.cli_password_manager.core.service.file.loader;

import com.manager.cli_password_manager.core.entity.Note;

import java.security.KeyStore;
import java.util.List;
import java.util.Map;

public interface Loader<T> {
    T loadFile();
    T loadFile(char[] password);
    void saveFile(KeyStore keyStore, char[] password);
    void saveFile(Map<String, List<Note>> data);
}
