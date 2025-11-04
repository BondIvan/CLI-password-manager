package com.manager.cli_password_manager.cli_ui.output.command;

import com.manager.cli_password_manager.cli_ui.output.ShellOutputHelper;
import com.manager.cli_password_manager.cli_ui.output.ShellInputHelper;
import com.manager.cli_password_manager.core.entity.converter.StringExportFormatConverter;
import com.manager.cli_password_manager.core.entity.enums.IngestionResult;
import com.manager.cli_password_manager.core.service.command.usecase.export.ExportCommand;
import com.manager.cli_password_manager.core.service.command.usecase.export.ExportFormat;
import com.manager.cli_password_manager.core.service.command.usecase.ingestion.IngestionCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@ShellCommandGroup("import/export")
@RequiredArgsConstructor
public class IOCommands {
    private final ExportCommand exportCommand;
    private final IngestionCommand ingestionCommand;

    private final ShellOutputHelper shellOutputHelper;
    private final ShellInputHelper shellInputHelper;

    private final StringExportFormatConverter stringExportFormatConverter;

    @ShellMethod(key = "export", value = "Export all services to a file")
    public String export(
            @ShellOption(arity = 1, value = {"--format", "-f"}) String format,
            @ShellOption(arity = 1, value = {"--protect", "-p"}, help = "Protect by password", defaultValue = "false") boolean isProtect
    ) {

        String passwordProtection = null;
        if(isProtect) {
            passwordProtection = shellInputHelper.readInput("Set the password protection - ", true);
        }

        ExportFormat exportFormat = stringExportFormatConverter.toExportFormat(format);

        exportCommand.execute(exportFormat, passwordProtection);

        return "Success";
    }

    @ShellMethod(key = "import", value = "Import data")
    public String importNotes(
            @ShellOption(arity = 1, value = {"--path", "-p"},
                    help = "Specify the absolute path to the file being imported here. If the path contains backslashes, " +
                            "replace them with forward slashes or duplicate them.") String path
    ) {
        shellOutputHelper.printInfo("Identical data will be replaced with new data.");

        IngestionResult result = ingestionCommand.execute(path, null);

        if(result == IngestionResult.PASSWORD_REQUIRED) {
            shellOutputHelper.printWarning("This file is password protected");
            String inputPassword = shellInputHelper.readInput("Please enter the password to access the file - ", true);
            result = ingestionCommand.execute(path, inputPassword);
        }

        return result == IngestionResult.SUCCESS ?
                "Success" :
                "Import failed. Check the password";
    }
}
