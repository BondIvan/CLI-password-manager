package com.manager.cli_password_manager.cli_ui.init;

import com.manager.cli_password_manager.cli_ui.output.ShellOutputHelper;
import com.manager.cli_password_manager.core.exception.security.CryptoAesOperationException;
import com.manager.cli_password_manager.core.exception.security.MasterPasswordException;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import com.manager.cli_password_manager.core.repository.VaultRepository;
import com.manager.cli_password_manager.core.service.clipboard.ClipboardService;
import com.manager.cli_password_manager.security.mp.MasterPasswordService;
import lombok.extern.slf4j.Slf4j;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Order(-1)
@Component
public class Initializer implements ApplicationRunner {
    private final ShellOutputHelper shellOutputHelper;
    private final LineReader lineReader;
    private final MasterPasswordService masterPasswordService;
    private final ApplicationContext applicationContext;
    private final VaultRepository vaultRepository;
    private final InMemoryNotesRepository notesRepository;
    private final ClipboardService clipboardService;

    public Initializer(
            MasterPasswordService masterPasswordService,
            VaultRepository vaultRepository,
            InMemoryNotesRepository notesRepository,
            ShellOutputHelper shellOutputHelper,
            @Lazy LineReader lineReader,
            ApplicationContext applicationContext,
            ClipboardService clipboardService) {
        this.masterPasswordService = masterPasswordService;
        this.vaultRepository = vaultRepository;
        this.notesRepository = notesRepository;
        this.shellOutputHelper = shellOutputHelper;
        this.lineReader = lineReader;
        this.applicationContext = applicationContext;
        this.clipboardService = clipboardService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if(!masterPasswordService.isExist()) {
            shellOutputHelper.print("Добро пожаловать!\nНеобходимо установить мастер-пароль.");

            setupNewMasterPassword();
        } else {
            login();
        }

        String cbStatus = clipboardService.isClipboardAvailable() ?
                "available" :
                "unavailable";

        shellOutputHelper.printInfo("Clipboard status - " + cbStatus);
    }

    private void setupNewMasterPassword() {
        String password;
        String confirmPassword;

        while(true) {
            try {
                password = lineReader.readLine("Введите мастер-пароль - ", '*');
                if (password == null || password.trim().isEmpty()) {
                    shellOutputHelper.printWarning("Мастер-пароль не может быть пустым.");
                    continue;
                }

                confirmPassword = lineReader.readLine("Подтвердите мастер-пароль - ", '*');
                if (password.equals(confirmPassword)) {
                    masterPasswordService.create(password);
                    shellOutputHelper.printSuccess("Мастер-пароль успешно установлен.");
                    vaultRepository.unlockVault(password.toCharArray());
                    log.info("Vault успешно создано.");
                    notesRepository.initialize();
                    log.info("Access успешно создано.");
                    return;
                } else {
                    shellOutputHelper.printError("Пароли не совпадают.\nПопробуйте еще раз.");
                }
            } catch (UserInterruptException e) {
                shellOutputHelper.printError("\nУстановка мастер-пароля прервана.");
                return;
            } catch (IOException e) {
                shellOutputHelper.printError("Ошибка файла мастер-пароля.");
                return;
            } catch (MasterPasswordException e) {
                shellOutputHelper.printError("Ошибка создания мастер пароля");
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
                shellOutputHelper.printError("\nВвод мастер-пароля прерван.");
                return;
            } catch (IOException e) {
                shellOutputHelper.printError("Ошибка файла мастер-пароля");
                return;
            } catch (CryptoAesOperationException e) {
                shellOutputHelper.printError("Ошибка обработки мастер-пароля");
                return;
            } catch (MasterPasswordException e) {
                maxAttempts--;
                shellOutputHelper.printWarning(String.format("Неверный мастер-пароль. Осталось попыток: %d\n", maxAttempts));
            }
        }

        //TODO Send email message - someone try to get your data (notification service needed)
        log.error("Invalid master password");
        shellOutputHelper.printError("Превышено число попыток. Приложение будет закрыто.\n");
        int exitCode = SpringApplication.exit(applicationContext, () -> 5);
        System.exit(exitCode);
    }
}
