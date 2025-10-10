package com.manager.cli_password_manager.core.service.command.usecase.replace.strategy;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.enums.ReplaceType;
import com.manager.cli_password_manager.core.service.command.usecase.replace.Replacement;
import com.manager.cli_password_manager.core.service.password.PasswordValidation;
import com.manager.cli_password_manager.security.encrypt.PasswordEncryptor;
import org.springframework.stereotype.Component;

@Component
public class PasswordReplacer implements Replacement {
    private final PasswordValidation passwordValidation;
    private final PasswordEncryptor passwordEncryptor;

    public PasswordReplacer(PasswordValidation passwordValidation,
                            PasswordEncryptor passwordEncryptor
    ) {
        this.passwordValidation = passwordValidation;
        this.passwordEncryptor = passwordEncryptor;
    }

    @Override
    public Note replace(Note replacingNote, String newPassword) {
//        if(!passwordValidation.isValid(newPassword))
//            throw new ReplaceValidationException("New password does not meet the requirements");

        //TODO Вернуть
        //TODO Проверить новый пароль через сервис HIBP
        //TODO Не забыть обновить данные кэша для hibp

        String encryptedNewPassword = passwordEncryptor.encryptPassword(replacingNote.getId(), newPassword); // This is also update key in keyStore
        return replacingNote.withPassword(encryptedNewPassword);
    }

    @Override
    public ReplaceType getReplaceType() {
        return ReplaceType.PASSWORD;
    }
}
