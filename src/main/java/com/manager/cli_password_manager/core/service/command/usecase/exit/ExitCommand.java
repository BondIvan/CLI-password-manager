package com.manager.cli_password_manager.core.service.command.usecase.exit;

import com.manager.cli_password_manager.core.service.clipboard.ClipboardService;
import com.manager.cli_password_manager.core.repository.InMemoryVaultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.commands.Quit;

@RequiredArgsConstructor
@ShellComponent
public class ExitCommand implements Quit.Command {
    private final ClipboardService clipboardService;
    private final InMemoryVaultRepository inMemoryVaultRepository;

    @ShellMethod(key = "exit", value = "Exit from cli application")
    public void exit() {
        clipboardService.clearClipboardBeforeShutdown();
        inMemoryVaultRepository.lockVault();
        throw new ExitRequest();
    }
}
