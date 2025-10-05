package com.manager.cli_password_manager.security.encrypt.aes;

import com.manager.cli_password_manager.core.entity.dto.encoder.EncryptionResult;
import com.manager.cli_password_manager.core.entity.dto.encoder.EncryptionResultWithKey;
import com.manager.cli_password_manager.core.entity.dto.export.AESExportParameters;
import com.manager.cli_password_manager.core.exception.security.EncryptionException;
import com.manager.cli_password_manager.security.EncryptionUtils;
import com.manager.cli_password_manager.security.encrypt.Encrypting;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AES_GCM implements Encrypting {
    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_ITERATIONS = 65536;
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int SALT_LENGTH = 16;
    public static final int GCM_IV_LENGTH = 12;

    private final SecureRandom secureRandom;

    public AES_GCM(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    @Override
    public AESExportParameters encryptData(String data, char[] password) {
        EncryptionResult result = encrypt(data, password);

        AESExportParameters params = new AESExportParameters("AES-GCM-256", result.base64EncryptedResult());
        params.setKdf("PBKDF2");
        params.setSalt(Base64.getEncoder().encodeToString(result.salt()));
        params.setIv(Base64.getEncoder().encodeToString(result.iv()));
        params.setIterations(KEY_ITERATIONS);

        return params;
    }

    @Override
    public EncryptionResultWithKey encryptPassword(String data) { //TODO Заменить на один метод с перегрузками
        EncryptionResult result = encrypt(data, data.toCharArray());
        return new EncryptionResultWithKey(
                result.base64EncryptedResult(),
                result.key()
        );
    }

    private EncryptionResult encrypt(String data, char[] password) {
        final byte[] salt = generateSalt();
        final byte[] iv = generateIV();
        SecretKey key = generateKey(password, salt);

        byte[] concatenatedIvAndEncrypted = doFinal(data, iv, key);
        String base64View = Base64.getEncoder().encodeToString(concatenatedIvAndEncrypted);
        EncryptionResult result = new EncryptionResult(base64View, key, salt, iv);

//        EncryptionUtils.clearData(salt, iv, concatenatedIvAndEncrypted); //TODO Нужно подумать

        return result;
    }

    @Override
    public byte[] encryptMasterPassword(String data, byte[] key, byte[] IV) {
        if(key == null || key.length != AES_KEY_SIZE / 8)
            throw new EncryptionException("Wrong key size");

        byte[] iv = (IV == null) ? generateIV() : IV; // Generating IV, required for AES algorithm in GCM mode
        SecretKey keySpec = new SecretKeySpec(key, "AES");
        byte[] concatenatedIvAndEncrypted = doFinal(data, iv, keySpec);

        EncryptionUtils.clearData(iv);

        return concatenatedIvAndEncrypted;
    }

    @Override
    public String decryptPassword(SecretKey key, String data) {
        byte[] fromBase64ToByteView = Base64.getDecoder().decode(data);
        byte[] iv = Arrays.copyOfRange(fromBase64ToByteView, 0, GCM_IV_LENGTH);
        byte[] encryptText = Arrays.copyOfRange(fromBase64ToByteView, GCM_IV_LENGTH, fromBase64ToByteView.length);

        byte[] decrypted;
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            decrypted = cipher.doFinal(encryptText);
        } catch (Exception e) {
            throw new EncryptionException("Decrypting error: ", e);
        }

        // Clearing sensitive data from memory
        EncryptionUtils.clearData(fromBase64ToByteView, iv, encryptText);

        return new String(decrypted);
    }

    private byte[] doFinal(String data, byte[] iv, SecretKey keySpec) {
        byte[] encrypted;
        byte[] concatenatedIvAndEncrypted;
        try {
            // Create an AES Cipher instance
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            // Concatenation of IV and encrypted data
            encrypted = cipher.doFinal(data.getBytes());
            concatenatedIvAndEncrypted = new byte[iv.length + encrypted.length];

            System.arraycopy(iv, 0, concatenatedIvAndEncrypted, 0, iv.length);
            System.arraycopy(encrypted, 0, concatenatedIvAndEncrypted, iv.length, encrypted.length);
        } catch (Exception e) {
            throw new EncryptionException("Encrypting error: " + e.getMessage(), e);
        }

        EncryptionUtils.clearData(encrypted);

        return concatenatedIvAndEncrypted;
    }

    // Generating an encryption key
    private SecretKey generateKey(char[] password, byte[] salt) {
        SecretKey tmp;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
            KeySpec spec = new PBEKeySpec(password, salt, KEY_ITERATIONS, AES_KEY_SIZE);
            tmp = factory.generateSecret(spec);
            Arrays.fill(password,'\0');
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new EncryptionException("Error while generating key for encrypt: " + e.getMessage(), e);
        }

        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return salt;
    }

    private byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        return iv;
    }
}
