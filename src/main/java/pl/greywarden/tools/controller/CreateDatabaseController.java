package pl.greywarden.tools.controller;

import com.google.common.eventbus.EventBus;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.tools.ValueExtractor;
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;
import org.springframework.stereotype.Controller;
import pl.greywarden.tools.component.EncryptionTypeComboBox;
import pl.greywarden.tools.component.IdGeneratorComboBox;
import pl.greywarden.tools.component.TextFieldWithDefaultValue;
import pl.greywarden.tools.model.InventoryItemColumn;
import pl.greywarden.tools.model.database.ColumnType;
import pl.greywarden.tools.model.event.request.CreateDatabaseRequest;
import pl.greywarden.tools.service.ApplicationSettingsService;
import pl.greywarden.tools.service.DatabaseService;
import pl.greywarden.tools.service.DialogService;
import pl.greywarden.tools.service.FileService;

import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;

@Controller
@RequiredArgsConstructor
public class CreateDatabaseController implements Initializable {
    private final EventBus eventBus;
    private final DatabaseService databaseService;
    private final FileService fileService;
    private final ApplicationSettingsService applicationSettingsService;
    private final DialogService dialogService;

    private final ValidationSupport validationSupport = new ValidationSupport();

    private final SimpleBooleanProperty dirtyProperty = new SimpleBooleanProperty(false);
    private final ResourceBundle resourceBundle;

    @FXML
    private Button createDatabaseButton;
    @FXML
    private IdGeneratorComboBox idGenerator;
    @FXML
    private VBox createDatabaseDialog;
    @FXML
    private PasswordField encryptionPasswordConfirmation;
    @FXML
    private EncryptionTypeComboBox encryptionType;
    @FXML
    private CheckBox enableEncryption;
    @FXML
    private PasswordField encryptionPassword;
    @FXML
    private TextField databaseName;
    @FXML
    private TextField newColumnName;
    @FXML
    private Button createColumnButton;
    @FXML
    private ComboBox<ColumnType> columnTypes;
    @FXML
    private TableView<InventoryItemColumn> databaseStructure;
    @FXML
    private TextFieldWithDefaultValue databaseDirectory;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idGenerator.setResourceBundle(resourceBundle);

        var defaultDatabasePath = applicationSettingsService.getDefaultDatabasePath();
        databaseDirectory.setDefaultValue(defaultDatabasePath);
        databaseDirectory.setText(defaultDatabasePath);
        databaseDirectory.textProperty().addListener(observable -> dirtyProperty.set(true));

        columnTypes.setItems(FXCollections.observableArrayList(ColumnType.values()));
        columnTypes.getSelectionModel().selectFirst();

        createColumnButton.disableProperty().bind(newColumnName.textProperty().isEmpty());

        databaseStructure.getItems().addListener((ListChangeListener<? super InventoryItemColumn>) c -> dirtyProperty.set(true));
        databaseName.textProperty().addListener(observable -> dirtyProperty.set(true));

        initializeValidation();
    }

    @SneakyThrows
    private void initializeValidation() {
        var decor = new StyleClassValidationDecoration("error", "error");
        validationSupport.setValidationDecorator(decor);
        ValueExtractor.addObservableValueExtractor(c -> c instanceof TableView<?>, c -> new SimpleListProperty<>(((TableView<?>) c).itemsProperty().getValue()));

        validationSupport.registerValidator(databaseName,
                Validator.createPredicateValidator(StringUtils::isNotEmpty,
                        resourceBundle.getString("create-database.validation-error.name-empty")));
        validationSupport.registerValidator(databaseDirectory,
                Validator.createPredicateValidator(databaseService::isValidDirectory,
                        resourceBundle.getString("create-database.validation-error.invalid-path")));
        validationSupport.registerValidator(databaseStructure,
                Validator.createPredicateValidator(CollectionUtils::isNotEmpty,
                        resourceBundle.getString("create-database.validation-error.empty-columns")));
        validationSupport.registerValidator(encryptionPassword,
                Validator.createPredicateValidator(this::validateEncryptionPassword,
                        resourceBundle.getString("create-database.validation-error.password-empty")));
        validationSupport.registerValidator(encryptionPasswordConfirmation,
                Validator.createPredicateValidator(this::validateEncryptionPasswordConfirmation,
                        resourceBundle.getString("create-database.validation-error.passwords-not-matching")));

        validationSupport.validationResultProperty().addListener((observable, oldValue, newValue) -> {
            validationSupport.getRegisteredControls().forEach(e -> e.setTooltip(null));
            var validationMessages = newValue.getMessages();
            validationMessages.forEach(this::installTooltip);
        });

        createDatabaseButton.disableProperty().bind(validationSupport.invalidProperty());
    }

    private void installTooltip(ValidationMessage validationMessage) {
        validationMessage.getTarget().setTooltip(new Tooltip(validationMessage.getText()));
    }

    private boolean validateEncryptionPasswordConfirmation(String passwordConfirmation) {
        return !enableEncryption.isSelected() || (StringUtils.isNotEmpty(passwordConfirmation) && StringUtils.equals(passwordConfirmation, encryptionPassword.getText()));
    }

    private boolean validateEncryptionPassword(String password) {
        return !enableEncryption.isSelected() || StringUtils.isNotEmpty(password);
    }

    @FXML
    private void selectDirectoryForDatabase() {
        var window = databaseDirectory.getScene().getWindow();
        var directoryChooser = new DirectoryChooser();
        var selectedDirectory = directoryChooser.showDialog(window);

        databaseDirectory.setText(selectedDirectory.getAbsolutePath());
    }

    @FXML
    private void createColumn() {
        var columnName = newColumnName.getText();
        var columnType = columnTypes.getSelectionModel().getSelectedItem();

        if (containsColumnWithName(columnName)) {
            var styleClass = new HashSet<>(newColumnName.getStyleClass());
            styleClass.add("error");
            newColumnName.getStyleClass().setAll(styleClass);
        } else {
            newColumnName.getStyleClass().removeIf("error"::equals);
            databaseStructure.itemsProperty().get().add(new InventoryItemColumn(newColumnName.getText(), columnType));
            newColumnName.setText(null);
            if (containsIdColumn()) {
                columnTypes.itemsProperty().get().remove(ColumnType.ID);
            }
            if (columnTypes.getSelectionModel().getSelectedItem() == null) {
                columnTypes.getSelectionModel().selectFirst();
            }
            newColumnName.requestFocus();
        }
    }

    @FXML
    private void newColumnNameEventHandler(KeyEvent keyEvent) {
        if (KeyCode.ENTER == keyEvent.getCode()) {
            if (StringUtils.isNotBlank(newColumnName.getText())) {
                createColumn();
            }
        }
    }

    @FXML
    private void databaseStructureEventHandler(KeyEvent keyEvent) {
        if (KeyCode.DELETE == keyEvent.getCode()) {
            var selectedItem = databaseStructure.getSelectionModel().getSelectedItem();
            databaseStructure.itemsProperty().get().remove(selectedItem);
            if (!containsIdColumn()) {
                columnTypes.setItems(FXCollections.observableArrayList(ColumnType.values()));
            }
        }
    }

    @FXML
    public void cancel() {
        if (dirtyProperty.get()) {
            dialogService.showConfirmationDialog(getStage(), "create-database.confirm-cancel", "create-database.confirm-cancel-title",
                    buttonType -> {
                        if (buttonType.equals(ButtonType.YES)) {
                            clearAndClose();
                        }
                    });
        } else {
            clearAndClose();
        }
    }

    @FXML
    private void createDatabase() {
        var databasePath = databaseService.getDatabasePath(databaseDirectory.getText(), databaseName.getText());
        if (fileService.hasReadWritePermission(databaseDirectory.getText())) {
            if (fileService.exists(databasePath)) {
                confirmDatabaseCreationIfFileExists(databasePath);
            } else {
                createAndPostDatabaseCreationRequest(databasePath);
                clearAndClose();
            }
        } else {
            dialogService.showWarningDialog(getStage(), "create-database.no-permission", "create-database.no-permission.title", databasePath);
        }
    }

    private void confirmDatabaseCreationIfFileExists(String databasePath) {
        dialogService.showConfirmationDialog(getStage(), "create-database.file-exists", "create-database.file-exists.title", buttonType -> {
            if (buttonType.equals(ButtonType.YES)) {
                createAndPostDatabaseCreationRequest(databasePath);
                clearAndClose();
            }
        }, databasePath);
    }

    private void createAndPostDatabaseCreationRequest(String databasePath) {
        var request = new CreateDatabaseRequest()
                .withDatabasePath(databasePath)
                .withIdGenerationStrategy(idGenerator.getSelectionModel().getSelectedItem())
                .withColumns(databaseStructure.getItems())
                .withEncryption(enableEncryption.isSelected());
        if (enableEncryption.isSelected()) {
            request = request
                    .withEncryptionType(encryptionType.getValue())
                    .withEncryptionPassword(encryptionPassword.getText());
        }
        eventBus.post(request);
        applicationSettingsService.setDefaultDatabasePath(databaseDirectory.getText());
    }

    private boolean containsIdColumn() {
        return databaseStructure.getItems().stream().map(InventoryItemColumn::getColumnType).anyMatch(ColumnType.ID::equals);
    }

    private boolean containsColumnWithName(String name) {
        return databaseStructure.getItems().stream().map(InventoryItemColumn::getColumnName).anyMatch(s -> s.equals(name));
    }

    private void closeWindow() {
        getStage().close();
    }

    private Stage getStage() {
        return (Stage) createDatabaseDialog.getScene().getWindow();
    }

    private void clearAndClose() {
        clear();
        closeWindow();
        dirtyProperty.set(false);
        validationSupport.initInitialDecoration();
    }

    private void clear() {
        databaseName.setText(null);
        databaseDirectory.setText(databaseDirectory.getDefaultValue());
        databaseStructure.itemsProperty().get().clear();
        encryptionPassword.setText(null);
        encryptionPasswordConfirmation.setText(null);

        newColumnName.setText(null);

        columnTypes.setItems(FXCollections.observableArrayList(ColumnType.values()));
        columnTypes.getSelectionModel().selectFirst();
    }
}
