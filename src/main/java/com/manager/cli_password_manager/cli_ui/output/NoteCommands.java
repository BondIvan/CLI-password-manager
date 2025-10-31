package com.manager.cli_password_manager.cli_ui.output;

import com.manager.cli_password_manager.cli_ui.consoleProgress.ConsoleProgressReporter;
import com.manager.cli_password_manager.core.entity.converter.StringCategoryConverter;
import com.manager.cli_password_manager.core.entity.converter.StringCheckingApiConverter;
import com.manager.cli_password_manager.core.entity.converter.StringExportFormatConverter;
import com.manager.cli_password_manager.core.entity.converter.StringReplaceTypeConverter;
import com.manager.cli_password_manager.core.entity.dto.checker.CheckerResult;
import com.manager.cli_password_manager.core.entity.dto.command.DecryptedNoteDTO;
import com.manager.cli_password_manager.core.entity.dto.command.InputAddDTO;
import com.manager.cli_password_manager.core.entity.dto.command.InputCheckDTO;
import com.manager.cli_password_manager.core.entity.dto.command.InputReplaceDTO;
import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;
import com.manager.cli_password_manager.core.entity.enums.Category;
import com.manager.cli_password_manager.core.entity.enums.CheckingApi;
import com.manager.cli_password_manager.core.entity.enums.IngestionResult;
import com.manager.cli_password_manager.core.entity.enums.ReplaceType;
import com.manager.cli_password_manager.core.entity.enums.SortType;
import com.manager.cli_password_manager.core.service.command.usecase.export.ExportFormat;
import com.manager.cli_password_manager.core.provider.CategoryValueProvider;
import com.manager.cli_password_manager.core.provider.ReplaceTypeValueProvider;
import com.manager.cli_password_manager.core.provider.ServiceNameValueProvider;
import com.manager.cli_password_manager.core.service.clipboard.ClipboardService;
import com.manager.cli_password_manager.core.service.command.usecase.add.AddCommand;
import com.manager.cli_password_manager.core.service.command.usecase.check.CheckCommand;
import com.manager.cli_password_manager.core.service.command.usecase.delete.DeleteCommand;
import com.manager.cli_password_manager.core.service.command.usecase.export.ExportCommand;
import com.manager.cli_password_manager.core.service.command.usecase.get.GetCommand;
import com.manager.cli_password_manager.core.service.command.usecase.getall.GetAllCommand;
import com.manager.cli_password_manager.core.service.command.usecase.ingestion.IngestionCommand;
import com.manager.cli_password_manager.core.service.command.usecase.replace.ReplaceCommand;
import org.jline.reader.LineReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@ShellComponent
public class NoteCommands { //TODO Сделать класс для отображения (note presenter). Нужно хорошенько подумать над его проектированием
    private final StringCategoryConverter stringCategoryConverter;
    private final DeleteCommand deleteCommand;
    private final StringReplaceTypeConverter stringReplaceTypeConverter;
    private final ReplaceCommand replaceCommand;
    private final StringCheckingApiConverter stringCheckingApiConverter;
    private final CheckCommand checkCommand;
    private final ConsoleProgressReporter consoleProgressReporter;
    private final ExportCommand exportCommand;
    private final StringExportFormatConverter stringExportFormatConverter;
    private final IngestionCommand ingestionCommand;

    @Value("${shell.clearClipboardAfterSeconds}")
    private long clearClipboardAfterSeconds;

    private final LineReader lineReader;
    private final ShellHelper shellHelper;
    private final ClipboardService clipboardService;

    private final AddCommand addCommand;
    private final GetAllCommand getAllCommand;
    private final GetCommand getCommand;

    public NoteCommands(
            @Lazy LineReader lineReader,
            AddCommand addCommand,
            GetAllCommand getAllCommand,
            ShellHelper shellHelper,
            GetCommand getCommand,
            ClipboardService clipboardService,
            StringCategoryConverter stringCategoryConverter,
            DeleteCommand deleteCommand,
            StringReplaceTypeConverter stringReplaceTypeConverter,
            ReplaceCommand replaceCommand,
            StringCheckingApiConverter stringCheckingApiConverter,
            CheckCommand checkCommand,
            ConsoleProgressReporter consoleProgressReporter,
            ExportCommand exportCommand,
            StringExportFormatConverter stringExportFormatConverter,
            IngestionCommand ingestionCommand) {
        this.lineReader = lineReader;
        this.shellHelper = shellHelper;
        this.clipboardService = clipboardService;
        this.addCommand = addCommand;
        this.getAllCommand = getAllCommand;
        this.getCommand = getCommand;
        this.stringCategoryConverter = stringCategoryConverter;
        this.deleteCommand = deleteCommand;
        this.stringReplaceTypeConverter = stringReplaceTypeConverter;
        this.replaceCommand = replaceCommand;
        this.stringCheckingApiConverter = stringCheckingApiConverter;
        this.checkCommand = checkCommand;
        this.consoleProgressReporter = consoleProgressReporter;
        this.exportCommand = exportCommand;
        this.stringExportFormatConverter = stringExportFormatConverter;
        this.ingestionCommand = ingestionCommand;
    }

    @ShellMethod(key = "add", value = "add a new service")
    public String add(
            @ShellOption(arity = 1, value = {"--name", "-n"}) String serviceName,
            @ShellOption(arity = 1, value = {"--login", "-l", "-lg"}) Optional<String> login,
            @ShellOption(arity = 1, value = {"--category", "-c"}, valueProvider = CategoryValueProvider.class, defaultValue = ShellOption.NULL) String category,
            @ShellOption(arity = 1, value = {"--generate", "-g"}, defaultValue = "false") boolean isAutoGeneratePassword,
            @ShellOption(arity = 1, value = {"--exclude", "-e"}, defaultValue = ShellOption.NULL) char[] excludedSymbols
    ) {
        if(login.isEmpty())
            return shellHelper.getErrorMessage("Missing mandatory option '--login'");

        String password = null;
        if(!isAutoGeneratePassword)
            password = lineReader.readLine("Please enter the password - ", '*');

        Category selectCategory = stringCategoryConverter.toCategory(category);

        InputAddDTO dto = new InputAddDTO(
                serviceName,
                login.get(),
                password,
                selectCategory,
                isAutoGeneratePassword,
                excludedSymbols
        );

        boolean executeResult = addCommand.execute(dto);

        return executeResult ? "Успешно добавлено" : "Ошибка добавления";
    }

    @ShellMethod(key = "get", value = "get service information. Use --login to specify if there are multiple entries.")
    public String get(
            @ShellOption(arity = 1, value = {"--name", "-n"}, valueProvider = ServiceNameValueProvider.class,
                    help = "Имя сервиса, для которого нужно получить данные.") String serviceName,
            @ShellOption(arity = 1, value = {"--login", "-l", "-lg"}, defaultValue = ShellOption.NULL,
                    help = "Логин для уточнения, если для сервиса несколько записей.") String login
    ) {
        List<DecryptedNoteDTO> notesByName = getCommand.getNotesByName(serviceName);

        if(notesByName.isEmpty())
            return shellHelper.getErrorMessage("Service with such name not found");

        if(notesByName.size() == 1) {
            DecryptedNoteDTO searched = notesByName.getFirst();
            if(clipboardService.isClipboardAvailable()) {
                clipboardService.copyToClipboard(searched.password());
                shellHelper.printInfo("Copied text will be removed after " + clearClipboardAfterSeconds + " seconds");
                return searched.displayWithoutPassword();
            }

            return searched.displayWithPassword();
        }

        if(login == null) {
            List<String[]> namePlusLogin = new ArrayList<>();

            for(DecryptedNoteDTO note: notesByName)
                namePlusLogin.add(new String[]{note.name() + " <-> ", note.login()});

            TableModel model = new ArrayTableModel(namePlusLogin.toArray(String[][]::new));
            TableBuilder tableBuilder = new TableBuilder(model);

            shellHelper.print(tableBuilder.build().render(200));

            return shellHelper.getWarningMessage("You have several services with such name. Please provide login for the desired service");
        }

        Optional<DecryptedNoteDTO> searchedNote = notesByName.stream()
                .filter(nt -> nt.login().equalsIgnoreCase(login))
                .findFirst();

        if(searchedNote.isEmpty())
            return shellHelper.getErrorMessage("Service with such login not found");

        DecryptedNoteDTO searched = searchedNote.get();
        if(clipboardService.isClipboardAvailable()) {
            clipboardService.copyToClipboard(searched.password());
            shellHelper.printInfo("Copied text will be removed after " + clearClipboardAfterSeconds + " seconds");
            return searched.displayWithoutPassword();
        }

        return searched.displayWithPassword();
    }

    @ShellMethod(key = "get-all", value = "get all service names")
    public void getAll(
            @ShellOption(arity = 1, value = {"--sort-by", "-s"}, defaultValue = "name") String sortBy
    ) {
        Map<String, List<NoteNamePlusLoginDTO>> sortedBy = getAllCommand.execute(sortBy);

        UnaryOperator<List<NoteNamePlusLoginDTO>> unaryOperator = list -> list.stream().distinct().toList();
        Function<NoteNamePlusLoginDTO, String> formatter = NoteNamePlusLoginDTO::name;
        if(SortType.fromString(sortBy) == SortType.CATEGORY) { //TODO Сделать StringSortTypeConverter
            formatter = dto -> String.format("%s (%s)", dto.name(), dto.login());
            unaryOperator = list -> list;
        }

        List<String[]> array2D = preparedDataForTable(sortedBy, formatter, unaryOperator);
        TableModel model = new ArrayTableModel(array2D.toArray(String[][]::new));
        TableBuilder tableBuilder = new TableBuilder(model);

        shellHelper.print(tableBuilder.build().render(200));
    }

    @ShellMethod(key = "delete", value = "Delete the service. Use --login to specify if there are multiple entries.")
    public String delete(
            @ShellOption(arity = 1, value = {"--name", "-n"}, valueProvider = ServiceNameValueProvider.class,
                    help = "Имя сервиса, который нужно удалить.") String serviceName,
            @ShellOption(arity = 1, value = {"--login", "-l", "-lg"}, defaultValue = ShellOption.NULL,
                    help = "Логин для уточнения, если для сервиса несколько записей.") String login
    ) {
        NoteNamePlusLoginDTO noteNamePlusLoginDTO = new NoteNamePlusLoginDTO(serviceName, login);
        boolean executeResult = deleteCommand.execute(noteNamePlusLoginDTO);

        if(executeResult)
            return "Delete was successfully";

        return "Not deleted";
    }

    @ShellMethod(key = "replace", value = "Change data for service. Use --login to specify if there are multiple entries.")
    public String replace(
            @ShellOption(arity = 1, value = {"--name", "-n"}, valueProvider = ServiceNameValueProvider.class,
                    help = "Имя сервиса, для которого нужно изменить данные.") String serviceName,
            @ShellOption(arity = 1, value = {"--login", "-l", "-lg"}, defaultValue = ShellOption.NULL,
                    help = "Логин для уточнения, если для сервиса несколько записей.") String login,
            @ShellOption(arity = 1, value = {"--type", "-t"}, valueProvider = ReplaceTypeValueProvider.class,
                    help = "Возможные варианты: name, login, category, password") Optional<String> replaceType,
            @ShellOption(arity = 1, value = {"--value", "-v"},
                    help = "Для изменения пароля не вводите пароль в самой команде") Optional<String> value
    ) {
        if(replaceType.isEmpty())
            return shellHelper.getErrorMessage("Missing mandatory option '--type'");

        ReplaceType type = stringReplaceTypeConverter.toReplaceType(replaceType.get());

        if(value.isEmpty() && type != ReplaceType.PASSWORD)
            return shellHelper.getErrorMessage("Missing mandatory option '--value'");

        String newValue;
        if(type == ReplaceType.PASSWORD)
            newValue = lineReader.readLine("Set the password - ", '*');
        else
            newValue = value.get();

        InputReplaceDTO inputReplaceDTO = new InputReplaceDTO(
                serviceName,
                login,
                type,
                newValue
        );

        boolean result = replaceCommand.execute(inputReplaceDTO);

        if(result)
            return "Replace was successfully";

        return "Not replaced";
    }

    @ShellMethod(key = "check", value = "Check password/passwords for compromise." +
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
            shellHelper.printWarning("Specified login without service name");

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
        Function<NoteNamePlusLoginDTO, String> formatter = dto -> String.format("%s (%s)", dto.name(), dto.login());
        UnaryOperator<List<NoteNamePlusLoginDTO>> unaryOperator = UnaryOperator.identity();

        List<String[]> array2D = preparedDataForTable(sortedByPwned, formatter, unaryOperator);
        TableModel model = new ArrayTableModel(array2D.toArray(String[][]::new));
        TableBuilder tableBuilder = new TableBuilder(model);

        shellHelper.print(tableBuilder.build().render(200));
    }

    @ShellMethod(key = "export", value = "Export all services to a file")
    public String export(
            @ShellOption(arity = 1, value = {"--format", "-f"}) String format,
            @ShellOption(arity = 1, value = {"--protect", "-p"}, help = "Protect by password", defaultValue = "false") boolean isProtect
    ) {
        String passwordProtection = null;
        if(isProtect)
            passwordProtection = lineReader.readLine("Set the password protection - ", '*');

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
        shellHelper.printInfo("Identical data will be replaced with new data.");

        IngestionResult result = ingestionCommand.execute(path, null);

        if(result == IngestionResult.PASSWORD_REQUIRED) {
            shellHelper.printWarning("This file is password protected");
            String inputPassword = lineReader.readLine("Please enter the password to access the file - ", '*');
            result = ingestionCommand.execute(path, inputPassword);
        }

        return result == IngestionResult.SUCCESS ?
                "Success" :
                "Import failed. Check the password";
    }

    private List<String[]> preparedDataForTable(Map<String, List<NoteNamePlusLoginDTO>> data,
                                                Function<NoteNamePlusLoginDTO, String> formatter,
                                                UnaryOperator<List<NoteNamePlusLoginDTO>> unaryOperator) {
        List<String[]> tableRows = new ArrayList<>();
        for(Map.Entry<String, List<NoteNamePlusLoginDTO>> entry: data.entrySet()) {
            String key_sortedBy = entry.getKey();
            List<NoteNamePlusLoginDTO> value_sortedBy = unaryOperator.apply(entry.getValue());

            tableRows.add(new String[]{key_sortedBy + ":"});
            value_sortedBy.forEach(item -> {
                String formattedStr = "\u00A0\u00A0 • " + formatter.apply(item);
                tableRows.add(new String[] {formattedStr});
            });
        }

        return tableRows;
    }
}
