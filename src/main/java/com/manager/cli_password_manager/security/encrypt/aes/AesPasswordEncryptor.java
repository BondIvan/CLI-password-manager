package com.manager.cli_password_manager.security.encrypt.aes;

import com.manager.cli_password_manager.core.repository.InMemoryVaultRepository;
import com.manager.cli_password_manager.security.encrypt.PasswordEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Base64;

@Service("aesPasswordEncryptor")
@RequiredArgsConstructor
public class AesPasswordEncryptor implements PasswordEncryptor {
    private final AesCryptoService aesCryptoService;
    private final InMemoryVaultRepository vaultRepository;

    @Override
    public String encryptPassword(String noteId, String servicePassword) {
        byte[] plainPassword = servicePassword.getBytes();
        byte[] salt = aesCryptoService.generateSalt();
        byte[] iv = aesCryptoService.generateIV();

        SecretKey secretKey = aesCryptoService.generateKey(servicePassword.toCharArray(), salt);
        vaultRepository.addKey(noteId, secretKey);

        byte[] cipherPassword = aesCryptoService.encrypt(plainPassword, secretKey, iv);
        byte[] concatenatedIvAndEncrypted = new byte[AesCryptoService.GCM_IV_LENGTH + cipherPassword.length];
        System.arraycopy(iv, 0, concatenatedIvAndEncrypted, 0, iv.length);
        System.arraycopy(cipherPassword, 0, concatenatedIvAndEncrypted, iv.length, cipherPassword.length);

        return Base64.getEncoder().encodeToString(concatenatedIvAndEncrypted);
    }

    @Override
    public String decryptPassword(String noteId, String servicePassword) {
        SecretKey secretKey = vaultRepository.getKey(noteId);
        byte[] cipherPassword = Base64.getDecoder().decode(servicePassword);
        byte[] ivFromCipher = Arrays.copyOfRange(cipherPassword, 0, AesCryptoService.GCM_IV_LENGTH);
        byte[] dataFromCipher = Arrays.copyOfRange(cipherPassword, AesCryptoService.GCM_IV_LENGTH, cipherPassword.length);
        byte[] decryptedPassword = aesCryptoService.decrypt(dataFromCipher, secretKey, ivFromCipher);

        return new String(decryptedPassword);
    }
}
