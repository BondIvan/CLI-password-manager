package com.manager.cli_password_manager.cli_ui;

import com.manager.cli_password_manager.cli_ui.output.ShellOutputHelper;
import com.manager.cli_password_manager.core.exception.IO.ingestion.IngestionCommandException;
import com.manager.cli_password_manager.core.exception.Initialization.InitializerException;
import com.manager.cli_password_manager.core.exception.clipboard.ClipboardException;
import com.manager.cli_password_manager.core.exception.command.AddCommandException;
import com.manager.cli_password_manager.core.exception.command.CheckCommandException;
import com.manager.cli_password_manager.core.exception.command.DeleteCommandException;
import com.manager.cli_password_manager.core.exception.command.ExportCommandException;
import com.manager.cli_password_manager.core.exception.command.GetAllCommandException;
import com.manager.cli_password_manager.core.exception.command.GetCommandException;
import com.manager.cli_password_manager.core.exception.command.ReplaceCommandException;
import com.manager.cli_password_manager.core.exception.command.ReplaceValidationException;
import com.manager.cli_password_manager.core.exception.file.loader.FileCreatorException;
import com.manager.cli_password_manager.core.exception.file.loader.FileLoaderException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.command.CommandExceptionResolver;
import org.springframework.shell.command.CommandHandlingResult;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommandExceptionHandler implements CommandExceptionResolver {
    private final ShellOutputHelper shellOutputHelper;

    public CommandExceptionHandler(ShellOutputHelper shellOutputHelper) {
        this.shellOutputHelper = shellOutputHelper;
    }

    @Override
    public CommandHandlingResult resolve(Exception ex) {
        if(ex instanceof InitializerException) {
            String message = shellOutputHelper.getErrorMessage(ex.getMessage() + "\n");
            System.out.println("CommandExceptionHandler->InitializerException:");
            return CommandHandlingResult.of(message, ((InitializerException) ex).getExitCode());
        }

        if(ex instanceof AddCommandException) {
            String message = shellOutputHelper.getWarningMessage(ex.getMessage()+"\n");
            System.out.println("CommandExceptionHandler->AddCommandException:");
            return CommandHandlingResult.of(message);
        }

        if(ex instanceof GetAllCommandException) {
            String message = shellOutputHelper.getWarningMessage(ex.getMessage() + "\n");
            System.out.println("CommandExceptionHandler->GetAllCommandException:");
            return CommandHandlingResult.of(message);
        }

        if(ex instanceof GetCommandException) {
            String message = shellOutputHelper.getWarningMessage(ex.getMessage() + "\n");
            System.out.println("CommandExceptionHandler->GetCommandException:");
            return CommandHandlingResult.of(message);
        }

        if(ex instanceof DeleteCommandException) {
            String message = shellOutputHelper.getWarningMessage(ex.getMessage() + "\n");
            System.out.println("CommandExceptionHandler->DeleteCommandException:");
            return CommandHandlingResult.of(message);
        }

        if(ex instanceof ReplaceCommandException) {
            String message = shellOutputHelper.getWarningMessage(ex.getMessage() + "\n");
            System.out.println("CommandExceptionHandler->ReplaceCommandException:");
            return CommandHandlingResult.of(message);
        }

        if(ex instanceof ReplaceValidationException) {
            String message = shellOutputHelper.getWarningMessage(ex.getMessage() + "\n");
            System.out.println("CommandExceptionHandler->ReplaceValidationException:");
            return CommandHandlingResult.of(message);
        }

        if(ex instanceof CheckCommandException) {
            String message = shellOutputHelper.getWarningMessage(ex.getMessage() + "\n");
            System.out.println("CommandExceptionHandler->CheckCommandException:");
            return CommandHandlingResult.of(message);
        }

        if(ex instanceof InterruptedException) {
            String message = shellOutputHelper.getErrorMessage(ex.getMessage() + "\n");
            System.out.println("CommandExceptionHandler->InterruptedException:");
            return CommandHandlingResult.of(message);
        }

        if(ex instanceof FileLoaderException) {
            String message = shellOutputHelper.getErrorMessage(ex.getMessage() + "\n");
            System.out.println("CommandExceptionHandler->FileLoaderException:");
            return CommandHandlingResult.of(message, -1);
        }

        if(ex instanceof FileCreatorException) {
            String message = shellOutputHelper.getErrorMessage(ex.getMessage() + "\n");
            System.out.println("CommandExceptionHandler->FileCreatorException");
            return CommandHandlingResult.of(message, -1);
        }

        if(ex instanceof ClipboardException) {
            String message = shellOutputHelper.getErrorMessage(ex.getMessage() + "\n");
            System.out.println("CommandExceptionHandler->ClipboardException");
            return CommandHandlingResult.of(message, -1);
        }

        if(ex instanceof ExportCommandException) {
            String message = shellOutputHelper.getErrorMessage(ex.getMessage() + "\n");
            System.out.println("CommandExceptionHandler->ExportCommandException");
            return CommandHandlingResult.of(message);
        }

        if(ex instanceof IngestionCommandException) {
            String message = shellOutputHelper.getErrorMessage(ex.getMessage() + "\n");
            System.out.println("CommandExceptionHandler->IngestionCommandException");
            return CommandHandlingResult.of(message);
        }

        //TODO Добавить остальные исключения

        return null; // If not my, pass on
    }
}
