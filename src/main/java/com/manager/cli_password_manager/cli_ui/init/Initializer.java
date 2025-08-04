package com.manager.cli_password_manager.cli_ui.init;

import com.manager.cli_password_manager.cli_ui.output.ShellHelper;
import com.manager.cli_password_manager.core.exception.Initialization.InitializerException;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import com.manager.cli_password_manager.core.repository.VaultRepository;
import com.manager.cli_password_manager.core.service.MP.MasterPasswordService;
import lombok.extern.slf4j.Slf4j;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(-1)
@Component
public class Initializer implements ApplicationRunner {
    private final ShellHelper shellHelper;
    private final LineReader lineReader;
    private final MasterPasswordService masterPasswordService;
    private final VaultRepository vaultRepository;
    private final InMemoryNotesRepository notesRepository;

    public Initializer(
            MasterPasswordService masterPasswordService,
            VaultRepository vaultRepository,
            InMemoryNotesRepository notesRepository,
            ShellHelper shellHelper,
            @Lazy LineReader lineReader) {
        this.masterPasswordService = masterPasswordService;
        this.vaultRepository = vaultRepository;
        this.notesRepository = notesRepository;
        this.shellHelper = shellHelper;
        this.lineReader = lineReader;
    }

    @Override
    public void run(ApplicationArguments args) {
        if(!masterPasswordService.isExist()) {
            shellHelper.print("Добро пожаловать!\nНеобходимо установить мастер-пароль.");

            setupNewMasterPassword();
        } else {
            login();
        }
    }

    private void setupNewMasterPassword() {
        String password;
        String confirmPassword;

        while(true) {
            try {
                password = lineReader.readLine("Введите мастер-пароль - ", '*');
                if (password == null || password.trim().isEmpty()) {
                    shellHelper.printWarning("Мастер-пароль не может быть пустым.");
                    continue;
                }

                confirmPassword = lineReader.readLine("Подтвердите мастер-пароль - ", '*');
                if (password.equals(confirmPassword)) {
                    masterPasswordService.createMasterPassword(password.toCharArray());
                    shellHelper.printSuccess("Мастер-пароль успешно установлен.");
                    vaultRepository.unlockVault(password.toCharArray());
                    log.info("Vault успешно создано.");
                    notesRepository.initialize();
                    log.info("Access успешно создано.");
                    return;
                } else {
                    shellHelper.printError("Пароли не совпадают.\nПопробуйте еще раз.");
                }
            } catch (UserInterruptException e) {
                shellHelper.printError("\nУстановка мастер-пароля прервана.");
                return;
            }
        }
    }

    private void login() {
        int maxAttempts = 3;

        while(maxAttempts > 0) {
            try {
                String password = lineReader.readLine("Введите мастер-пароль для разблокировки: ", '*');
                if (masterPasswordService.verify(password)) {
                    vaultRepository.unlockVault(password.toCharArray());
                    log.info("Vault успешно загружено.");
                    notesRepository.initialize();
                    log.info("Access успешно загружено.");
                    return;
                } else {
                    maxAttempts--;
                    shellHelper.printWarning(String.format("Неверный мастер-пароль. Осталось попыток: %d\n", maxAttempts));
                }
            } catch (UserInterruptException e) {
                shellHelper.printError("\nВвод мастер-пароля прерван.");
                return;
            }
        }

        //TODO Send email message - someone try to get your data (notification service needed)
        log.error("Invalid master password");
        throw new InitializerException("Invalid password");
    }
}
