package com.manager.cli_password_manager.cli_ui.output.command;

import com.manager.cli_password_manager.cli_ui.consoleProgress.ConsoleProgressReporter;
import com.manager.cli_password_manager.cli_ui.output.ShellOutputHelper;
import com.manager.cli_password_manager.cli_ui.output.TableUtils;
import com.manager.cli_password_manager.core.entity.converter.StringCheckingApiConverter;
import com.manager.cli_password_manager.core.entity.dto.checker.CheckerResult;
import com.manager.cli_password_manager.core.entity.dto.command.InputCheckDTO;
import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;
import com.manager.cli_password_manager.core.entity.enums.CheckingApi;
import com.manager.cli_password_manager.core.service.command.usecase.check.CheckCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@ShellComponent
@ShellCommandGroup("security")
@RequiredArgsConstructor
public class SecurityCommands {
    private final CheckCommand checkCommand;

    private final ShellOutputHelper shellOutputHelper;

    private final ConsoleProgressReporter consoleProgressReporter;
    private final StringCheckingApiConverter stringCheckingApiConverter;
    private final TableUtils tableUtils;

    @ShellMethod(key = "check", value = "Check password/passwords for compromise. " +
            "If you do not specify a service, all are checked by default.")
    public void check(
            @ShellOption(arity = 1, value = "-hibp", defaultValue = "hibp") String apiType,
            @ShellOption(arity = 1, value = {"--name", "-n"}, defaultValue = ShellOption.NULL) Optional<String> serviceName,
            @ShellOption(arity = 1, value = {"--login", "-l", "-lg"}, defaultValue = ShellOption.NULL) Optional<String> login
    ) {
        System.out.println("apiType: " + apiType);
        if(apiType == null) //TODO apiType по умолчанию не ставится (null) - разобраться позже
            apiType = "hibp";

        CheckingApi checkingApi = stringCheckingApiConverter.toCheckingApi(apiType);

        if(serviceName.isEmpty() && login.isPresent())
            shellOutputHelper.printWarning("Specified login without service name");

        InputCheckDTO inputCheckDTO = new InputCheckDTO(
                checkingApi,
                serviceName,
                login
        );

        List<CheckerResult> result = checkCommand.execute(inputCheckDTO, consoleProgressReporter);

        Map<String, List<NoteNamePlusLoginDTO>> sortedByPwned = result.stream()
                .collect(Collectors.groupingBy(
                        o -> o.isPwned() ? "Compromised" : "Secure",
                        Collectors.mapping(CheckerResult::namePlusLoginDTO, Collectors.toList())
                ));
        Function<NoteNamePlusLoginDTO, String> formatter = dto -> String.format("%s (%s)",
                dto.name(), dto.login());
        UnaryOperator<List<NoteNamePlusLoginDTO>> unaryOperator = UnaryOperator.identity();

        List<String[]> array2D = tableUtils.preparedDataForTable(sortedByPwned, formatter, unaryOperator);
        TableModel model = new ArrayTableModel(array2D.toArray(String[][]::new));
        TableBuilder tableBuilder = new TableBuilder(model);

        shellOutputHelper.print(tableBuilder.build().render(200));
    }
}
