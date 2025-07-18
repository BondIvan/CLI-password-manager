package com.manager.cli_password_manager.core.service.command.usecase.replace.strategy;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.encoder.EncryptionResult;
import com.manager.cli_password_manager.core.entity.dto.replacer.ReplacementResult;
import com.manager.cli_password_manager.core.entity.enums.ReplaceType;
import com.manager.cli_password_manager.core.service.command.usecase.replace.Replacement;
import com.manager.cli_password_manager.core.service.password.PasswordValidation;
import com.manager.cli_password_manager.security.encrypt.aes.AES_GCM;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PasswordReplacer implements Replacement {
    private final PasswordValidation passwordValidation;
    private final AES_GCM aesGcm;

    public PasswordReplacer(PasswordValidation passwordValidation,
                            AES_GCM aesGcm
    ) {
        this.passwordValidation = passwordValidation;
        this.aesGcm = aesGcm;
    }

    @Override
    public ReplacementResult replace(Note replacingNote, String newPassword) {
//        if(!passwordValidation.isValid(newPassword))
//            throw new ReplaceValidationException("New password does not meet the requirements");

        //TODO Вернуть
        //TODO Проверить новый пароль через сервис HIBP
        //TODO Не забыть обновить данные кэша для hibp

        EncryptionResult encryptedNewPassword = aesGcm.encryptPassword(newPassword);

        return new ReplacementResult(
                replacingNote.withPassword(encryptedNewPassword.encryptedPassword()),
                Optional.of(encryptedNewPassword.key())
        );
    }

    @Override
    public ReplaceType getReplaceType() {
        return ReplaceType.PASSWORD;
    }
}
