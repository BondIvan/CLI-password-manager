package com.manager.cli_password_manager.security.encrypt.aes;

import com.manager.cli_password_manager.core.exception.security.CryptoAesOperationException;
import com.manager.cli_password_manager.core.exception.security.MasterPasswordException;
import com.manager.cli_password_manager.security.hash.Hashing;
import com.manager.cli_password_manager.security.mp.MasterPasswordAssembly;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.crypto.AEADBadTagException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

@Service
public class AesMasterPasswordService implements MasterPasswordAssembly {
    private final AesCryptoService aesCryptoService;
    @Qualifier("argon2")
    private final Hashing hashing;

    public AesMasterPasswordService(AesCryptoService aesCryptoService,
                                    Hashing hashing) {
        this.aesCryptoService = aesCryptoService;
        this.hashing = hashing;
    }

    @Override
    public byte[] encodeMasterPassword(String masterPassword) {
        try {
            byte[] salt = aesCryptoService.generateSalt();
            byte[] iv = aesCryptoService.generateIV();
            byte[] hashKey = hashing.hashWithSalt(masterPassword, salt);
            SecretKey secretKey = new SecretKeySpec(hashKey, "AES");

            byte[] encryptedValidation = aesCryptoService.encrypt("validation".getBytes(), secretKey, iv);
            byte[] concatenatedIvAndEncrypted = new byte[AesCryptoService.GCM_IV_LENGTH + encryptedValidation.length];
            System.arraycopy(iv, 0, concatenatedIvAndEncrypted, 0, iv.length);
            System.arraycopy(encryptedValidation, 0, concatenatedIvAndEncrypted, iv.length, encryptedValidation.length);

            byte[] saltPlusConcatenated = new byte[salt.length + concatenatedIvAndEncrypted.length];
            System.arraycopy(salt, 0, saltPlusConcatenated, 0, salt.length);
            System.arraycopy(concatenatedIvAndEncrypted, 0, saltPlusConcatenated, salt.length, concatenatedIvAndEncrypted.length);

            return saltPlusConcatenated;
        } catch (CryptoAesOperationException e) {
            throw new MasterPasswordException("Setup master password failed", e);
        }
    }

    @Override
    public byte[] decodeMasterPassword(byte[] masterPasswordBytes, String inputPassword) {
        try {
            byte[] salt = Arrays.copyOfRange(masterPasswordBytes, 0, AesCryptoService.SALT_LENGTH);
            byte[] ivPlusValidation = Arrays.copyOfRange(masterPasswordBytes, AesCryptoService.SALT_LENGTH, masterPasswordBytes.length);
            byte[] iv = Arrays.copyOfRange(ivPlusValidation, 0, AesCryptoService.GCM_IV_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(ivPlusValidation, AesCryptoService.GCM_IV_LENGTH, ivPlusValidation.length);
            byte[] hashKey = hashing.hashWithSalt(inputPassword, salt);
            SecretKey secretKey = new SecretKeySpec(hashKey, "AES");

            return aesCryptoService.decrypt(cipherText, secretKey, iv);
        } catch (CryptoAesOperationException e) {
            Throwable cause = e.getCause();
            if(cause instanceof AEADBadTagException)
                throw new MasterPasswordException(cause);

            throw e;
        }
    }
}
