package pl.greywarden.tools.controller;

import com.google.common.eventbus.EventBus;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import pl.greywarden.tools.component.EncryptionTypeComboBox;
import pl.greywarden.tools.component.PasswordFieldWithValidation;
import pl.greywarden.tools.component.TableViewWithValidation;
import pl.greywarden.tools.component.TextFieldWithValidation;
import pl.greywarden.tools.model.database.ColumnType;
import pl.greywarden.tools.model.event.CreateDatabaseRequest;
import pl.greywarden.tools.model.InventoryItemColumn;
import pl.greywarden.tools.service.ApplicationSettingsService;
import pl.greywarden.tools.service.DatabaseService;
import pl.greywarden.tools.service.FileService;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.function.Consumer;

@Controller
@RequiredArgsConstructor
public class CreateDatabaseController implements Initializable {
    public static final String DEFAULT_DATABASE_PATH_PROPERTY = "default-database-path";

    private final EventBus eventBus;
    private final DatabaseService databaseService;
    private final FileService fileService;
    private final ApplicationSettingsService applicationSettingsService;

    private final SimpleBooleanProperty dirtyProperty = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty isValidProperty = new SimpleBooleanProperty(false);
    private ResourceBundle resourceBundle;

    @FXML
    private VBox createDatabaseDialog;
    @FXML
    private PasswordFieldWithValidation encryptionPasswordConfirmation;
    @FXML
    private EncryptionTypeComboBox encryptionType;
    @FXML
    private CheckBox enableEncryption;
    @FXML
    private PasswordFieldWithValidation encryptionPassword;
    @FXML
    private TextFieldWithValidation databaseName;
    @FXML
    private TextField newColumnName;
    @FXML
    private Button createColumnButton;
    @FXML
    private ComboBox<ColumnType> columnTypes;
    @FXML
    private TableViewWithValidation<InventoryItemColumn> databaseStructure;
    @FXML
    private TextFieldWithValidation databaseDirectory;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resourceBundle = resources;

        var defaultDatabasePath = applicationSettingsService.getString(DEFAULT_DATABASE_PATH_PROPERTY, System.getProperty("user.home"));
        databaseDirectory.setDefaultValue(defaultDatabasePath);
        databaseDirectory.setText(defaultDatabasePath);
        databaseDirectory.textProperty().addListener(observable -> dirtyProperty.set(true));

        columnTypes.setItems(FXCollections.observableArrayList(ColumnType.values()));
        columnTypes.getSelectionModel().selectFirst();

        createColumnButton.disableProperty().bind(newColumnName.textProperty().isEmpty());

        databaseStructure.getItems().addListener((ListChangeListener<? super InventoryItemColumn>) change -> dirtyProperty.set(true));
        databaseName.textProperty().addListener(observable -> dirtyProperty.set(true));

        isValidProperty.bind(
                databaseName.validProperty()
                        .and(databaseDirectory.validProperty()
                                .and(databaseStructure.validProperty()
                                        .and(encryptionPassword.validProperty()))));
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
            var styleClass = newColumnName.getStyleClass();
            if (!styleClass.contains("error")) {
                newColumnName.getStyleClass().add("error");
            }
        } else {
            newColumnName.getStyleClass().remove("error");
            databaseStructure.getItems().add(new InventoryItemColumn(newColumnName.getText(), columnType));
            newColumnName.setText(null);
            if (containsIdColumn()) {
                columnTypes.getItems().remove(ColumnType.ID);
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
            databaseStructure.getItems().remove(selectedItem);
            if (!containsIdColumn()) {
                columnTypes.setItems(FXCollections.observableArrayList(ColumnType.values()));
            }
        }
    }

    @FXML
    private void cancel() {
        if (dirtyProperty.get()) {
            showConfirmationDialog("create-database.confirm-cancel", "create-database.confirm-cancel-title",
                    buttonType -> {
                        if (buttonType.equals(ButtonType.YES)) {
                            clearAndClose();
                        }
                    });
        } else {
            clearAndClose();
        }
    }

    private void showConfirmationDialog(String messageKey, String titleKey, Consumer<ButtonType> handler, Object... args) {
        var formatter = new MessageFormat("");
        formatter.applyPattern(resourceBundle.getString(messageKey));
        var message = formatter.format(args);
        var alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(resourceBundle.getString(titleKey));
        alert.setHeaderText(null);
        alert.setGraphic(null);
        bringAlertTop(alert);
        alert.showAndWait().ifPresent(handler);
    }

    @FXML
    private void createDatabase() {
        validateInput();
        var databasePath = databaseService.getDatabasePath(databaseDirectory.getText(), databaseName.getText());
        if (isValidProperty.get()) {
            if (fileService.hasReadWritePermission(databaseDirectory.getText())) {
                if (fileService.exists(databasePath)) {
                    showConfirmationDialog("create-database.file-exists", "create-database.file-exists.title", buttonType -> {
                        if (buttonType.equals(ButtonType.YES)) {
                            createAndPostDatabaseCreationRequest(databasePath);
                            clearAndClose();
                        }
                    }, databasePath);
                } else {
                    createAndPostDatabaseCreationRequest(databasePath);
                    clearAndClose();
                }
            } else {
                showWarningDialog("create-database.no-permission", "create-database.no-permission.title", databasePath);
            }
        } else {
            showWarningDialog("create-database.validation-error", "create-database.validation-error.title");
        }
    }

    private void createAndPostDatabaseCreationRequest(String databasePath) {
        var request = new CreateDatabaseRequest()
                .withDatabasePath(databasePath)
                .withColumns(databaseStructure.getItems())
                .withEncryption(enableEncryption.isSelected());
        if (enableEncryption.isSelected()) {
            request = request.withEncryptionType(encryptionType.getValue());
        }
        eventBus.post(request);
        applicationSettingsService.setProperty(DEFAULT_DATABASE_PATH_PROPERTY, databaseDirectory.getText());
        databaseDirectory.setDefaultValue(databaseDirectory.getText());
    }

    private void bringAlertTop(Alert alert) {
        var stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(getStage());
        stage.setAlwaysOnTop(true);
        stage.setOnShown(event -> stage.requestFocus());
    }

    private void validateInput() {
        databaseName.validate(StringUtils::isNotEmpty, resourceBundle.getString("create-database.validation-error.name-empty"));
        databaseDirectory.validate(databaseService::isValidDirectory, resourceBundle.getString("create-database.validation-error.invalid-path"));
        databaseStructure.validate(CollectionUtils::isNotEmpty, resourceBundle.getString("create-database.validation-error.empty-columns"));
        encryptionPassword.validate(this::validateEncryptionPassword, resourceBundle.getString("create-database.validation-error.password-empty"));
        encryptionPasswordConfirmation.validate(this::validateEncryptionPasswordConfirmation, resourceBundle.getString("create-database.validation-error.passwords-not-matching"));
    }

    private Boolean validateEncryptionPassword(String password) {
        if (enableEncryption.isSelected()) {
            return StringUtils.isNotEmpty(password);
        }
        return true;
    }

    private Boolean validateEncryptionPasswordConfirmation(String password) {
        if (enableEncryption.isSelected()) {
            var encryptionPassword = this.encryptionPassword.getText();
            var encryptionPasswordConfirmation = this.encryptionPasswordConfirmation.getText();
            return StringUtils.isNotEmpty(encryptionPassword)
                    && StringUtils.isNotEmpty(encryptionPasswordConfirmation)
                    && encryptionPasswordConfirmation.equals(encryptionPassword);
        }
        return true;
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
    }

    private void clear() {
        databaseName.invalidate();
        databaseDirectory.invalidate();
        databaseStructure.invalidate();

        newColumnName.setText(null);

        columnTypes.setItems(FXCollections.observableArrayList(ColumnType.values()));
        columnTypes.getSelectionModel().selectFirst();
    }

    private void showWarningDialog(String messageKey, String titleKey, Object... args) {
        var formatter = new MessageFormat("");
        formatter.applyPattern(resourceBundle.getString(messageKey));
        var errorMessage = formatter.format(args);
        var alert = new Alert(Alert.AlertType.WARNING, errorMessage);
        alert.setHeaderText(null);
        alert.setTitle(resourceBundle.getString(titleKey));
        bringAlertTop(alert);
        alert.show();
    }

}
