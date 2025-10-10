package com.manager.cli_password_manager.security.hash.argon2;

import com.manager.cli_password_manager.core.exception.security.HashingEncryption;
import com.manager.cli_password_manager.security.EncryptionUtils;
import com.manager.cli_password_manager.security.hash.Hashing;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.springframework.stereotype.Component;

@Component("argon2")
public class Argon2 implements Hashing {
    private static final int ITERATIONS = 2; //TODO Позже увеличить до 15
    private static final int MEM_LIMIT = 65536;
    private static final int PARALLELISM = 1;
    private static final int HASH_LENGTH = 32;
    public static final int SALT_LENGTH = 16;

    @Override
    public byte[] hashWithSalt(String data, byte[] salt) {
        return hashWithSalt(data.getBytes(), salt);
    }

    @Override
    public byte[] hashWithSalt(byte[] data, byte[] salt) {
        if(salt == null || salt.length != SALT_LENGTH)
            throw new HashingEncryption("Wrong salt parameter");

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(createParameters(salt));

        byte[] outBytes = new byte[HASH_LENGTH];

        generator.generateBytes(data, outBytes, 0, outBytes.length);

        EncryptionUtils.clearData(data);

        return outBytes;
    }

    private Argon2Parameters createParameters(byte[] salt) {
        return new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(ITERATIONS)
                .withMemoryAsKB(MEM_LIMIT)
                .withParallelism(PARALLELISM)
                .withSalt(salt)
                .build();
    }
}
