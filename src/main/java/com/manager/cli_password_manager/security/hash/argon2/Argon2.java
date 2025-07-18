package com.manager.cli_password_manager.security.hash.argon2;

import com.manager.cli_password_manager.core.exception.security.HashingEncryption;
import com.manager.cli_password_manager.security.EncryptionUtils;
import com.manager.cli_password_manager.security.hash.Hashing;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

@Component
public class Argon2 implements Hashing {
    private static final int ITERATIONS = 2; //TODO Позже увеличить до 15
    private static final int MEM_LIMIT = 65536;
    private static final int PARALLELISM = 1;
    private static final int HASH_LENGTH = 32;
    public static final int SALT_LENGTH = 16;

    private final SecureRandom secureRandom;

    public Argon2(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    @Override
    public byte[] hashWithSalt(String data, byte[] salt) {
        if(salt == null || salt.length != SALT_LENGTH)
            throw new HashingEncryption("Wrong salt parameter");

        Argon2Parameters parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(ITERATIONS)
                .withMemoryAsKB(MEM_LIMIT)
                .withParallelism(PARALLELISM)
                .withSalt(salt)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(parameters);

        byte[] outBytes = new byte[HASH_LENGTH];
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

        generator.generateBytes(dataBytes, outBytes, 0, outBytes.length);

        EncryptionUtils.clearData(dataBytes);

        return outBytes;
    }

    @Override
    public byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return salt;
    }
}
