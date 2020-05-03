package pl.greywarden.tools.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import pl.greywarden.tools.component.ButtonWithIcon;
import pl.greywarden.tools.component.MenuItemWithIcon;
import pl.greywarden.tools.component.PasswordInputDialog;
import pl.greywarden.tools.component.columns.BooleanTableColumn;
import pl.greywarden.tools.component.columns.IdTableColumn;
import pl.greywarden.tools.component.columns.NumberTableColumn;
import pl.greywarden.tools.component.columns.TextTableColumn;
import pl.greywarden.tools.listener.EventListener;
import pl.greywarden.tools.model.database.Column;
import pl.greywarden.tools.model.database.Database;
import pl.greywarden.tools.model.database.DatabaseContent;
import pl.greywarden.tools.model.event.request.CreateDatabaseRecordRequest;
import pl.greywarden.tools.model.event.request.LoadDatabaseFromFileRequest;
import pl.greywarden.tools.model.event.request.LoadDatabaseRequest;
import pl.greywarden.tools.model.event.request.SaveDatabaseRequest;
import pl.greywarden.tools.model.event.response.CreateDatabaseRecord;
import pl.greywarden.tools.service.ApplicationSettingsService;
import pl.greywarden.tools.service.DialogService;
import pl.greywarden.tools.service.EncryptionService;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static javafx.stage.FileChooser.ExtensionFilter;

@Controller
@EventListener
@RequiredArgsConstructor
public class MainWindowController implements Initializable {
    private final ConfigurableApplicationContext springContext;
    private final EncryptionService encryptionService;
    private final ApplicationSettingsService applicationSettingsService;
    private final DialogService dialogService;
    private final ResourceBundle resourceBundle;

    private final StringProperty databasePath = new SimpleStringProperty();
    private final ObjectProperty<Database> database = new SimpleObjectProperty<>();
    private final BooleanProperty dirtyProperty = new SimpleBooleanProperty(false);
    private final ObjectProperty<File> initialDirectoryProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<List<Column>> columns = new SimpleObjectProperty<>(new ArrayList<>());

    @FXML
    private TableView<ObservableMap<String, Object>> databaseContent;
    @FXML
    private MenuItemWithIcon saveDatabaseMenuItem;
    @FXML
    private VBox controlPanel;
    @FXML
    private VBox mainWindow;
    @FXML
    private ButtonWithIcon saveDatabaseButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        databaseContent.setEditable(true);

        saveDatabaseMenuItem.disableProperty().bind(dirtyProperty.not());
        saveDatabaseButton.disableProperty().bind(dirtyProperty.not());
        initializeMainWindow();
        addDirtyPropertyListener();
        configureInitialDirectory();
    }

    @Subscribe
    public void loadDatabase(LoadDatabaseRequest loadDatabaseRequest) {
        var database = loadDatabaseRequest.getDatabase();
        var content = database.getDatabaseContent();
        var encryption = database.isEncryption();
        if (encryption && content instanceof String) {
            var decryptedContent = decryptDatabaseContent(database);
            if (decryptedContent == null) {
                return;
            }
            database.setDatabaseContent(decryptedContent);
        }

        databaseContent.getItems().clear();
        databaseContent.getColumns().clear();
        this.columns.get().clear();

        var columns = database.<DatabaseContent>getDatabaseContent().getColumns();
        for (var column : columns) {
            switch (column.getType()) {
                case ID:
                    databaseContent.getColumns().add(new IdTableColumn(column.getName()));
                    break;
                case TEXT:
                    databaseContent.getColumns().add(new TextTableColumn(column.getName()));
                    break;
                case NUMBER:
                    databaseContent.getColumns().add(new NumberTableColumn(column.getName()));
                    break;
                case BOOLEAN:
                    databaseContent.getColumns().add(new BooleanTableColumn(column.getName()));
                    break;
            }
            this.columns.get().add(column);
        }
        for (var entry: database.<DatabaseContent>getDatabaseContent().getData()) {
            var observable = FXCollections.observableMap(entry);
            databaseContent.getItems().add(observable);
        }

        for (var column: databaseContent.getColumns()) {
            column.addEventHandler(TableColumn.editCommitEvent(), event -> dirtyProperty.set(true));
        }

        controlPanel.setDisable(false);
        databaseContent.setDisable(false);
        this.database.setValue(database);
        this.databasePath.setValue(database.getPath());
        setCleanTitle();
    }

    @Subscribe
    public void addDatabaseRecord(CreateDatabaseRecord request) {
        this.databaseContent.getItems().add(request.getDatabaseRecord());
        dirtyProperty.set(true);
    }

    @FXML
    public void exit() {
        if (dirtyProperty.get()) {
            showConfirmExitDialog();
        }
        Platform.exit();
    }

    private void showConfirmExitDialog() {
        var eventBus = getEventBus();

        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        var closeWithoutSave = new ButtonType("Close without save");
        var cancel = new ButtonType("Cancel");
        var save = new ButtonType("Save");
        alert.getDialogPane().getButtonTypes().setAll(closeWithoutSave, cancel, save);
        alert.showAndWait().ifPresent(selectedOption -> {
            if (selectedOption == closeWithoutSave) {
                Platform.exit();
            } else if (selectedOption == save) {
                eventBus.post(new SaveDatabaseRequest(database.get()));
                Platform.exit();
            }
        });
    }

    @FXML
    private void createNewDatabase() {
        var createDatabaseDialog = springContext.getBean("createDatabaseDialog", Stage.class);
        if (!createDatabaseDialog.isShowing()) {
            createDatabaseDialog.show();
            createDatabaseDialog.requestFocus();
        }
    }

    @FXML
    private void createEntry() {
        var eventBus = getEventBus();
        var columns = this.columns.get();
        var idGenerationStrategy = this.database.get().<DatabaseContent>getDatabaseContent().getIdGenerationStrategy();
        var items = databaseContent.getItems();
        eventBus.post(new CreateDatabaseRecordRequest(columns, idGenerationStrategy,  items));
    }

    @FXML
    private void loadDatabase() {
        var eventBus = getEventBus();

        var fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(initialDirectoryProperty.get());
        fileChooser.getExtensionFilters().add(new ExtensionFilter(resourceBundle.getString("main-window.file-chooser.db-file-type"), "*.db"));
        fileChooser.getExtensionFilters().add(new ExtensionFilter(resourceBundle.getString("main-window.file-chooser.all-files"), "*"));

        var databaseFile = fileChooser.showOpenDialog(null);
        if (databaseFile != null) {
            initialDirectoryProperty.setValue(databaseFile.getParentFile());
            var loadDatabaseFromFile = new LoadDatabaseFromFileRequest(databaseFile);
            eventBus.post(loadDatabaseFromFile);
        }
    }

    @FXML
    private void saveDatabase() {
        var eventBus = getEventBus();
        var saveDatabaseRequest = new SaveDatabaseRequest(database.get());

        eventBus.post(saveDatabaseRequest);
        dirtyProperty.set(false);
    }

    private DatabaseContent decryptDatabaseContent(Database database) {
        var encryptedContent = database.<String>getDatabaseContent();
        var encryptionType = database.getEncryptionType();

        var dialog = new PasswordInputDialog(resourceBundle.getString("main-window.password-prompt.password"));
        dialog.setTitle(resourceBundle.getString("main-window.password-prompt.title"));

        var password = dialog.showAndWait().orElse(null);
        if (password != null) {
            try {
                var decryptedContent = encryptionService.decryptDatabaseContent(encryptedContent, password, encryptionType);
                database.setEncryptionPassword(password);
                return decryptedContent;
            } catch (Exception e) {
                showDecryptionFailedAlert();
                return decryptDatabaseContent(database);
            }
        } else {
            return null;
        }
    }

    private void showDecryptionFailedAlert() {
        var alert = new Alert(Alert.AlertType.WARNING);
        alert.getDialogPane().setContent(new Label(resourceBundle.getString("main-window.password-prompt.decryption-failed")));
        dialogService.bringAlertTop(getWindow(), alert);
        alert.showAndWait();
    }

    private Stage getWindow() {
        return (Stage) mainWindow.getScene().getWindow();
    }

    private void setCleanTitle() {
        var stage = getWindow();
        var databasePath = this.databasePath.get();
        var applicationName = "Inventory Manager";

        stage.setTitle(String.format("%s [%s]", applicationName, databasePath));
    }

    private void setDirtyTitle() {
        var stage = getWindow();
        var databasePath = this.databasePath.get();
        var applicationName = "Inventory Manager";

        stage.setTitle(String.format("%s [%s*]", applicationName, databasePath));
    }

    private void addDirtyPropertyListener() {
        dirtyProperty.addListener(event -> {
            if (((SimpleBooleanProperty) event).get()) {
                var items = databaseContent.getItems();
                this.database.get().<DatabaseContent>getDatabaseContent().setData(new ArrayList<>(items));
                setDirtyTitle();
            } else {
                setCleanTitle();
            }
        });
    }

    private void configureInitialDirectory() {
        initialDirectoryProperty.addListener(observable -> {
            var newInitialDirectory = (SimpleObjectProperty<?>) observable;
            applicationSettingsService.setInitialDirectory(newInitialDirectory.getValue().toString());
        });
        initialDirectoryProperty.setValue(new File(applicationSettingsService.getInitialDirectory()));
    }

    private void initializeMainWindow() {
        mainWindow.setPrefWidth(applicationSettingsService.getPrefWidth());
        mainWindow.setPrefHeight(applicationSettingsService.getPrefHeight());
        mainWindow.widthProperty().addListener(observable -> {
            if (!getWindow().isMaximized()) {
                var newWidth = (ReadOnlyDoubleProperty) observable;
                applicationSettingsService.setPrefWidth(newWidth.get());
            }
        });
        mainWindow.heightProperty().addListener(observable -> {
            if (!getWindow().isMaximized()) {
                var newHeight = (ReadOnlyDoubleProperty) observable;
                applicationSettingsService.setPrefHeight(newHeight.get());
            }
        });
    }

    private EventBus getEventBus() {
        return springContext.getBean(EventBus.class);
    }
}
