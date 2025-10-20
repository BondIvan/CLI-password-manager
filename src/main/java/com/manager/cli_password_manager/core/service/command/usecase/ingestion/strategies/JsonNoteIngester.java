package com.manager.cli_password_manager.core.service.command.usecase.ingestion.strategies;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.converter.StringCategoryConverter;
import com.manager.cli_password_manager.core.entity.converter.StringIOEncryptorAlgorithm;
import com.manager.cli_password_manager.core.entity.dto.export.EncryptedExportContainer;
import com.manager.cli_password_manager.core.entity.enums.Category;
import com.manager.cli_password_manager.core.entity.enums.IOEncryptorAlgorithm;
import com.manager.cli_password_manager.core.entity.enums.IngestionFormat;
import com.manager.cli_password_manager.core.exception.IO.IngestionException;
import com.manager.cli_password_manager.core.exception.IO.IngestionFileProtectedException;
import com.manager.cli_password_manager.core.exception.security.CryptoAesOperationException;
import com.manager.cli_password_manager.core.service.command.usecase.ingestion.IngestionContext;
import com.manager.cli_password_manager.core.service.command.usecase.ingestion.NoteIngester;
import com.manager.cli_password_manager.core.service.export.IOEncryptionService;
import com.manager.cli_password_manager.security.encrypt.PasswordEncryptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JsonNoteIngester implements NoteIngester {
    private final ObjectMapper objectMapper;
    private final IOEncryptionService ioEncryptionService;
    private final StringCategoryConverter categoryConverter;
    @Qualifier("aesPasswordEncryptor")
    private final PasswordEncryptor passwordEncryptor;
    private final StringIOEncryptorAlgorithm stringIOEncryptorAlgorithm;

    public JsonNoteIngester(ObjectMapper objectMapper,
                            IOEncryptionService ioEncryptionService,
                            StringCategoryConverter categoryConverter,
                            PasswordEncryptor passwordEncryptor,
                            StringIOEncryptorAlgorithm stringIOEncryptorAlgorithm) {
        this.objectMapper = objectMapper.copy();
        this.ioEncryptionService = ioEncryptionService;
        this.categoryConverter = categoryConverter;
        this.passwordEncryptor = passwordEncryptor;
        this.stringIOEncryptorAlgorithm = stringIOEncryptorAlgorithm;
    }

    @Override
    public Map<String, List<Note>> importNotes(IngestionContext context, InputStream inputStream) {
        if(!inputStream.markSupported())
            inputStream = new BufferedInputStream(inputStream); // for mark/reset opportunity

        inputStream.mark(1024 * 1024); // 1 MB (reading more will remove the mark)

        try {
            IOEncryptorAlgorithm isEncrypted = isDataEncrypted(inputStream);
            inputStream.reset(); // to the start of the file

            if(isEncrypted != null) {
                if(context.password() == null)
                    throw new IngestionFileProtectedException();

                EncryptedExportContainer container = objectMapper.readValue(inputStream, EncryptedExportContainer.class);
                return decryptedImport(container, context.password().toCharArray());
            }

            return plainImport(inputStream);
        } catch (IllegalArgumentException e) {
            throw new IngestionException("Json unknown algorithm", e);
        } catch (IOException e) {
            throw new IngestionException("Json import exception: " + e.getMessage(), e);
        }
    }

    private IOEncryptorAlgorithm isDataEncrypted(InputStream inputStream) throws IOException {
        JsonParser parser = objectMapper.createParser(inputStream);
        String anchor = "algorithm";

        if(parser.nextToken() != JsonToken.START_OBJECT)
            return null;

        while(parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.currentName();
            if(fieldName.equals(anchor)) {
                parser.nextToken();
                return stringIOEncryptorAlgorithm.toIOAlgorithmFormat(parser.getText());
            }

            parser.nextToken();
            parser.skipChildren();
        }

        return null;
    }

    private Map<String, List<Note>> decryptedImport(EncryptedExportContainer container, char[] password) throws IOException {
        try {
            byte[] bytes = ioEncryptionService.importData(
                    container.cipherText(),
                    container.saltBase64View(),
                    container.ivBase64View(),
                    IOEncryptorAlgorithm.AES_GCM_256,
                    password
            );

            try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                return plainImport(bais);
            }
        } catch (IngestionFileProtectedException e) {
            throw new IngestionException("Wrong password");
        } catch (CryptoAesOperationException e) {
            throw new IngestionException("Unknown decryption exception", e);
        }
    }

    private Map<String, List<Note>> plainImport(InputStream inputStream) throws IOException {
        log.info("Start analysing json plain data...");
        Map<String, List<Note>> importedFromFile = new HashMap<>();
        try (JsonParser parser = objectMapper.createParser(inputStream)) {
            while(parser.nextToken() != null) {
                if(parser.currentToken() == JsonToken.START_OBJECT)
                    continue;

                String fieldServiceName = parser.currentName();

                if(parser.nextToken() == JsonToken.START_ARRAY) {
                    List<Note> list = new ArrayList<>();
                    while(parser.nextToken() != JsonToken.END_ARRAY) {
                        if(parser.currentToken() != JsonToken.START_OBJECT) // skip '{' for each note
                            continue;

                        Note note = new Note();
                        while(parser.nextToken() != JsonToken.END_OBJECT) {
                            String partOfNoteName = parser.currentName();
                            parser.nextToken(); // move to value token
                            switch (partOfNoteName) {
                                case "name" -> {
                                    note.setName(parser.getText());
                                }
                                case "login" -> {
                                    note.setLogin(parser.getText());
                                }
                                case "category" -> {
                                    Category category = categoryConverter.toCategory(parser.getText());
                                    note.setCategory(category);
                                }
                                case "password" -> {
                                    // It might be slow because of this.
                                    String encryptedPassword = passwordEncryptor.encryptPassword(note.getId(), parser.getText());
                                    note.setPassword(encryptedPassword);
                                }
                            }
                        }

                        list.add(note);
                    }

                    importedFromFile.put(fieldServiceName, list);
                }
            }

        }

        log.info("End analysing json plain data.");
        return importedFromFile;
    }

    @Override
    public IngestionFormat getFormat() {
        return IngestionFormat.JSON;
    }
}
