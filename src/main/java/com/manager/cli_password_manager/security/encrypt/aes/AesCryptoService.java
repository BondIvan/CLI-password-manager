package com.manager.cli_password_manager.security.encrypt.aes;

import com.manager.cli_password_manager.core.exception.security.CryptoAesOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class AesCryptoService {
    private static final String MODE = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_ITERATIONS = 65536;
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 128;
    public static final int SALT_LENGTH = 16;
    public static final int GCM_IV_LENGTH = 12;

    private final SecureRandom secureRandom;

    public byte[] encrypt(byte[] plainText, SecretKey secretKey, byte[] iv) {
        try {
            Cipher cipher = createCipher(Cipher.ENCRYPT_MODE, secretKey, iv);
            return cipher.doFinal(plainText);
        } catch (GeneralSecurityException e) {
            log.error("Encrypt aes exception", e);
            throw new CryptoAesOperationException("Encrypt aes exception", e);
        }
    }

    public byte[] decrypt(byte[] cipherText, SecretKey secretKey, byte[] iv) {
        try {
            Cipher cipher = createCipher(Cipher.DECRYPT_MODE, secretKey, iv);
            return cipher.doFinal(cipherText);
        } catch (AEADBadTagException e) {
            log.error("Decryption failed: authentication tag mismatch. Likely wrong key or tampered data", e);
            throw new CryptoAesOperationException("Decryption failed: authentication tag mismatch. Likely wrong key or tampered data", e);
        } catch (GeneralSecurityException e) {
            log.error("Decryption failed due to a security configuration issue", e);
            throw new CryptoAesOperationException("Decryption failed due to a security configuration issue", e);
        }
    }

    public CipherOutputStream createEncryptingStream(OutputStream outputStream, char[] password, byte[] salt, byte[] iv) {
        try {
            final SecretKey secretKey = generateKey(password, salt);
            Cipher cipher = createCipher(Cipher.ENCRYPT_MODE, secretKey, iv);

            return new CipherOutputStream(outputStream, cipher);
        } catch (GeneralSecurityException e) {
            log.error("Fail creating encrypting stream", e);
            throw new CryptoAesOperationException("Fail creating encrypting stream", e);
        }
    }

    public CipherInputStream createDecryptingStream(InputStream inputStream, char[] password, byte[] salt, byte[] iv) {
        try {
            final SecretKey secretKey = generateKey(password, salt);
            Cipher cipher = createCipher(Cipher.DECRYPT_MODE, secretKey, iv);

            return new CipherInputStream(inputStream, cipher);
        } catch (GeneralSecurityException e) {
            log.error("Fail creating decrypting stream", e);
            throw new CryptoAesOperationException("Fail creating decrypting stream", e);
        }
    }

    private Cipher createCipher(int mode, SecretKey secretKey, byte[] iv) throws GeneralSecurityException {
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance(MODE);
        cipher.init(mode, secretKey, gcmParameterSpec);
        return cipher;
    }

    public SecretKey generateKey(char[] password, byte[] salt) {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
            KeySpec spec = new PBEKeySpec(password, salt, KEY_ITERATIONS, AES_KEY_SIZE);
            SecretKey secretKey = keyFactory.generateSecret(spec);

            return new SecretKeySpec(secretKey.getEncoded(), "AES");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error generating encrypt key", e);
            throw new CryptoAesOperationException("Error generating encrypt key", e);
        } finally {
            Arrays.fill(password,'\0');
        }
    }

    public byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return salt;
    }

    public byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        return iv;
    }
}
