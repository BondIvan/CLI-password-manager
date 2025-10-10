package com.manager.cli_password_manager.cli_ui.init;

import com.manager.cli_password_manager.cli_ui.output.ShellHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import com.manager.cli_password_manager.core.exception.security.CryptoAesOperationException;
import com.manager.cli_password_manager.core.exception.security.MasterPasswordException;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import com.manager.cli_password_manager.core.repository.VaultRepository;
import com.manager.cli_password_manager.security.mp.MasterPasswordService;
import lombok.extern.slf4j.Slf4j;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Order(-1)
@Component
public class Initializer implements ApplicationRunner {
    private final ShellHelper shellHelper;
    private final LineReader lineReader;
    private final MasterPasswordService masterPasswordService;
    private final ApplicationContext applicationContext;
    private final VaultRepository vaultRepository;
    private final InMemoryNotesRepository notesRepository;

    public Initializer(
            MasterPasswordService masterPasswordService,
            VaultRepository vaultRepository,
            InMemoryNotesRepository notesRepository,
            ShellHelper shellHelper,
            @Lazy LineReader lineReader,
            ApplicationContext applicationContext) {
        this.masterPasswordService = masterPasswordService;
        this.vaultRepository = vaultRepository;
        this.notesRepository = notesRepository;
        this.shellHelper = shellHelper;
        this.lineReader = lineReader;
        this.applicationContext = applicationContext;
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
                    masterPasswordService.create(password);
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
            } catch (IOException e) {
                shellHelper.printError("Ошибка файла мастер-пароля.");
                return;
            } catch (MasterPasswordException e) {
                shellHelper.printError("Ошибка создания мастер пароля");
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
                }
            } catch (UserInterruptException e) {
                shellHelper.printError("\nВвод мастер-пароля прерван.");
                return;
            } catch (IOException e) {
                shellHelper.printError("Ошибка файла мастер-пароля");
                return;
            } catch (CryptoAesOperationException e) {
                shellHelper.printError("Ошибка обработки мастер-пароля");
                return;
            } catch (MasterPasswordException e) {
                maxAttempts--;
                shellHelper.printWarning(String.format("Неверный мастер-пароль. Осталось попыток: %d\n", maxAttempts));
            }
        }

        //TODO Send email message - someone try to get your data (notification service needed)
        log.error("Invalid master password");
        shellHelper.printError("Превышено число попыток. Приложение будет закрыто.\n");
        int exitCode = SpringApplication.exit(applicationContext, () -> 5);
        System.exit(exitCode);
    }
}
