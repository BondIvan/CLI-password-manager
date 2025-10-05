package com.manager.cli_password_manager.core.service.command.usecase.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.export.AbstractExportParameters;
import com.manager.cli_password_manager.core.exception.command.ExportCommandException;
import com.manager.cli_password_manager.core.export.ExportFormat;
import com.manager.cli_password_manager.core.export.ExporterFactory;
import com.manager.cli_password_manager.core.export.NoteExporter;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import com.manager.cli_password_manager.core.repository.InMemoryVaultRepository;
import com.manager.cli_password_manager.core.service.file.creator.SecureFileCreator;
import com.manager.cli_password_manager.security.encrypt.Encrypting;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExportCommand {
    private final InMemoryNotesRepository notesRepository;
    private final InMemoryVaultRepository vaultRepository;
    private final ExporterFactory exporterFactory;
    private final SecureFileCreator fileCreator;
    private final Encrypting encrypting;
    private final ObjectMapper objectMapper;

    @Value("${shell.file.userHome}")
    private String userHome;
    @Value("${shell.export.path}")
    private String exportPath;
    @Value("${shell.export.name}")
    private String exportFileName;

    private Path exportFilePath;

    public ExportCommand(InMemoryNotesRepository notesRepository,
                         InMemoryVaultRepository vaultRepository,
                         ExporterFactory exporterFactory,
                         SecureFileCreator fileCreator,
                         Encrypting encrypting,
                         ObjectMapper objectMapper) {
        this.notesRepository = notesRepository;
        this.vaultRepository = vaultRepository;
        this.exporterFactory = exporterFactory;
        this.fileCreator = fileCreator;
        this.encrypting = encrypting;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        String homePath = System.getProperty(userHome);
        Path userDownloads = Paths.get(homePath, exportPath);
        this.exportFilePath = userDownloads.resolve(exportFileName);
    }

    public void execute(ExportFormat format, String passwordProtection) {
        NoteExporter noteExporter = exporterFactory.getExporter(format)
                .orElseThrow(() -> new ExportCommandException("Exporter for this format not found"));

        try {
            fileCreator.createAndSecure(exportFilePath);

            if (passwordProtection != null)
                withProtection(noteExporter, passwordProtection.toCharArray());
            else
                withoutProtection();
        } catch (Exception e) {
            throw new RuntimeException("Some export exception: " + e.getMessage());
        }
    }

    //{
    //  "version": "1.0",
    //  "encryptionAlgorithm": "AES-256-GCM",
    //  "kdf": "PBKDF2",
    //  "kdfParams": {
    //    "iterations": 150000,
    //    "salt": "Base64_encoded_random_salt_here"
    //  },
    //  "iv": "Base64_encoded_random_iv_here",
    //  "ciphertext": "Base64_encoded_encrypted_data_here"
    //}

    private Map<String, List<Note>> decryptAllNotes() {
        Map<String, List<Note>> notes = notesRepository.getAllNotes();
        Map<String, List<Note>> deepCopyNotes = notes.entrySet().stream()
                .collect(Collectors.toMap( // deep copying
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(Note::new) // copying constructor
                                .map(note -> {
                                    SecretKey key = vaultRepository.getKey(note.getId());
                                    String decrypted = encrypting.decryptPassword(key, note.getPassword());
                                    note.setPassword(decrypted);
                                    return note;
                                })
                                .toList(),
                        (e1, e2) -> e1,
                        HashMap::new
                ));

        return deepCopyNotes;
    }

    private void withProtection(NoteExporter noteExporter, char[] password) throws Exception {
        String exportData = noteExporter.export(decryptAllNotes());
        AbstractExportParameters abstractExportParameters = encrypting.encryptData(exportData, password);

        objectMapper.writeValue(exportFilePath.toFile(), abstractExportParameters);
    }

    private void withoutProtection() throws Exception {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(exportFilePath.toFile(), decryptAllNotes());
    }
}
