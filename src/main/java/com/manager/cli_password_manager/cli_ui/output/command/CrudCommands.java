package com.manager.cli_password_manager.cli_ui.output.command;

import com.manager.cli_password_manager.cli_ui.output.ShellInputHelper;
import com.manager.cli_password_manager.cli_ui.output.ShellOutputHelper;
import com.manager.cli_password_manager.cli_ui.output.TableUtils;
import com.manager.cli_password_manager.core.entity.converter.StringCategoryConverter;
import com.manager.cli_password_manager.core.entity.converter.StringReplaceTypeConverter;
import com.manager.cli_password_manager.core.entity.dto.command.DecryptedNoteDTO;
import com.manager.cli_password_manager.core.entity.dto.command.InputAddDTO;
import com.manager.cli_password_manager.core.entity.dto.command.InputReplaceDTO;
import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;
import com.manager.cli_password_manager.core.entity.enums.Category;
import com.manager.cli_password_manager.core.entity.enums.ReplaceType;
import com.manager.cli_password_manager.core.entity.enums.SortType;
import com.manager.cli_password_manager.core.provider.CategoryValueProvider;
import com.manager.cli_password_manager.core.provider.ReplaceTypeValueProvider;
import com.manager.cli_password_manager.core.provider.ServiceNameValueProvider;
import com.manager.cli_password_manager.core.service.clipboard.ClipboardService;
import com.manager.cli_password_manager.core.service.command.usecase.add.AddCommand;
import com.manager.cli_password_manager.core.service.command.usecase.delete.DeleteCommand;
import com.manager.cli_password_manager.core.service.command.usecase.get.GetCommand;
import com.manager.cli_password_manager.core.service.command.usecase.getall.GetAllCommand;
import com.manager.cli_password_manager.core.service.command.usecase.replace.ReplaceCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

@ShellComponent
@ShellCommandGroup("crud")
@RequiredArgsConstructor
public class CrudCommands {
    private final GetCommand getCommand;
    private final AddCommand addCommand;
    private final GetAllCommand getAllCommand;
    private final DeleteCommand deleteCommand;
    private final ReplaceCommand replaceCommand;

    private final ShellOutputHelper shellOutputHelper;
    private final ShellInputHelper shellInputHelper;

    private final ClipboardService clipboardService;
    private final StringCategoryConverter stringCategoryConverter;
    private final StringReplaceTypeConverter stringReplaceTypeConverter;
    private final TableUtils tableUtils;
    private final ConsoleTimerReporter consoleTimerReporter;

    @Value("${clipboard.clearAfterSeconds}")
    private long clearClipboardAfterSeconds;

    @ShellMethod(key = "get", value = "get service information. Use --login to specify if there are multiple entries." +
            "Use -n/--name for autocompletion for service name")
    public String get(
            @ShellOption(arity = 1, value = {"--name", "-n"}, valueProvider = ServiceNameValueProvider.class,
                    help = "Имя сервиса, для которого нужно получить данные.") String serviceName,
            @ShellOption(arity = 1, value = {"--login", "-l", "-lg"}, defaultValue = ShellOption.NULL,
                    help = "Логин для уточнения, если для сервиса несколько записей.") String login
    ) {
        List<DecryptedNoteDTO> notesByName = getCommand.getNotesByName(serviceName);

        if(notesByName.isEmpty())
            return shellOutputHelper.getErrorMessage("Service with such name not found");

        if(notesByName.size() == 1)
            return processFoundNote(notesByName.getFirst());

        if(login == null)
            return handleSeveralNoteAccounts(notesByName);

        Optional<DecryptedNoteDTO> searchedNote = notesByName.stream()
                .filter(nt -> nt.login().equalsIgnoreCase(login))
                .findFirst();

        return searchedNote
                .map(this::processFoundNote)
                .orElse(shellOutputHelper.getErrorMessage("Service with such login not found"));
    }

    private String processFoundNote(DecryptedNoteDTO dtoNote) {
        if(clipboardService.isClipboardAvailable()) {
            clipboardService.copyToClipboard(dtoNote.password());
            shellOutputHelper.printInfo("Copied text will be removed after " + clearClipboardAfterSeconds + " seconds");

            return dtoNote.displayWithoutPassword();
        }

        shellOutputHelper.print(dtoNote.displayWithPassword());

        timer();
        hide();

        return "";
    }

    private String handleSeveralNoteAccounts(List<DecryptedNoteDTO> notesByName) {
        List<NoteNamePlusLoginDTO> namePlusLoginDTOs = notesByName.stream()
                .map(note -> new NoteNamePlusLoginDTO(note.name(), note.login()))
                .toList();
        Function<NoteNamePlusLoginDTO, String> formatter = dto -> String.format("%s <-> %s", dto.name(), dto.login());
        UnaryOperator<List<NoteNamePlusLoginDTO>> unaryOperator = UnaryOperator.identity();

        List<String[]> array2D = tableUtils.preparedDataForTable(
                Map.of(namePlusLoginDTOs.getFirst().name(), namePlusLoginDTOs),
                formatter,
                unaryOperator
        );

        TableModel model = new ArrayTableModel(array2D.toArray(String[][]::new));
        TableBuilder tableBuilder = new TableBuilder(model);

        shellOutputHelper.print(tableBuilder.build().render(200));

        return shellOutputHelper.getWarningMessage("You have several services with such name. " +
                "Please provide login for the desired service");
    }

    private void timer() {
        try {
            for (int i = consoleTimerReporter.getTime(); i > 0; i--) {
                consoleTimerReporter.report(i, "seconds");
                Thread.sleep(1000);
            }
            consoleTimerReporter.complete("seconds");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            consoleTimerReporter.error("Timer was interrupted");
        }
    }

    private void hide() {
        shellOutputHelper.printWithoutLineBreak("\r");
        shellOutputHelper.printWithoutLineBreak("\033[K");

        shellOutputHelper.printWithoutLineBreak("\033[1A");
        shellOutputHelper.printWithoutLineBreak("\r");
        shellOutputHelper.printWithoutLineBreak("\033[K");
        shellOutputHelper.printWithoutLineBreak("Password: *****");
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

        List<String[]> array2D = tableUtils.preparedDataForTable(sortedBy, formatter, unaryOperator);
        TableModel model = new ArrayTableModel(array2D.toArray(String[][]::new));
        TableBuilder tableBuilder = new TableBuilder(model);

        shellOutputHelper.print(tableBuilder.build().render(200));
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
            return shellOutputHelper.getErrorMessage("Missing mandatory option '--login'");

        String password = null;
        if(!isAutoGeneratePassword)
            password = shellInputHelper.readInput("Please enter the password - ", true);

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
            return shellOutputHelper.getErrorMessage("Missing mandatory option '--type'");

        ReplaceType type = stringReplaceTypeConverter.toReplaceType(replaceType.get());

        if(value.isEmpty() && type != ReplaceType.PASSWORD)
            return shellOutputHelper.getErrorMessage("Missing mandatory option '--value'");

        String newValue;
        if(type == ReplaceType.PASSWORD)
            newValue = shellInputHelper.readInput("Set the password - ", true);
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
}
