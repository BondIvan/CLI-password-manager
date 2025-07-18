package com.manager.cli_password_manager.core.entity.dto.replacer;

import com.manager.cli_password_manager.core.entity.Note;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Optional;

public record ReplacementResult(
    Note newNote,
    Optional<SecretKey> key
) {
    @Override
    public String toString() {
        SecretKey k = key.orElseThrow(() -> new RuntimeException("key = null"));
        return "ReplacementResult{" +
                "newNote=" + newNote +
                ", key=" + Arrays.toString(k.getEncoded()) +
                '}';
    }
}
