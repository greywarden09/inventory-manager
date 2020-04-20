package pl.greywarden.tools.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import pl.greywarden.tools.EncryptionType;
import pl.greywarden.tools.component.DatabaseTableView;
import pl.greywarden.tools.component.PasswordInputDialog;
import pl.greywarden.tools.component.columns.BooleanTableColumn;
import pl.greywarden.tools.component.columns.IdTableColumn;
import pl.greywarden.tools.listener.EventListener;
import pl.greywarden.tools.model.database.Database;
import pl.greywarden.tools.model.database.DatabaseContent;
import pl.greywarden.tools.model.event.LoadDatabaseFromFile;
import pl.greywarden.tools.model.event.LoadDatabaseRequest;
import pl.greywarden.tools.service.EncryptionService;

import java.net.URL;
import java.util.ResourceBundle;

import static javafx.stage.FileChooser.ExtensionFilter;

@Controller
@EventListener
@RequiredArgsConstructor
public class MainWindowController implements Initializable {
    private final ConfigurableApplicationContext springContext;
    private final EncryptionService encryptionService;
    public DatabaseTableView databaseContent;
    private final ObjectProperty<Database> database = new SimpleObjectProperty<>();

    @FXML
    private VBox controlPanel;
    @FXML
    private VBox mainWindow;

    private ResourceBundle resourceBundle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        databaseContent.setEditable(true);
        this.resourceBundle = resources;
    }

    @FXML
    public void exit() {
        Platform.exit();
    }

    @FXML
    private void createNewDatabase() {
        var createDatabaseDialog = springContext.getBean("createDatabaseDialog", Stage.class);
        if (!createDatabaseDialog.isShowing()) {
            createDatabaseDialog.show();
            createDatabaseDialog.requestFocus();
        }
    }


    @Subscribe
    public void loadDatabase(LoadDatabaseRequest loadDatabaseRequest) {
        var database = loadDatabaseRequest.getDatabase();
        var content = database.getDatabaseContent();
        var encryption = database.isEncryption();
        if (encryption && content instanceof String) {
            var decryptedContent = decryptDatabaseContent(database.getDatabaseContent(), database.getEncryptionType());
            if (decryptedContent == null) {
                return;
            }
            database.setDatabaseContent(decryptedContent);
        }

        databaseContent.getItems().clear();
        databaseContent.getColumns().clear();

        this.database.setValue(database);

        var columns = database.<DatabaseContent>getDatabaseContent().getColumns();
        for (var column : columns) {
            switch (column.getType()) {
                case ID:
                    databaseContent.getColumns().add(new IdTableColumn(column.getName()));
                    break;
                case TEXT:
                    break;
                case NUMBER:
                    break;
                case BOOLEAN:
                    databaseContent.getColumns().add(new BooleanTableColumn(column.getName()));
                    break;
            }
        }
        for (var entry: database.<DatabaseContent>getDatabaseContent().getData()) {
            var observable = FXCollections.observableMap(entry);
            databaseContent.getItems().add(observable);
        }

        controlPanel.setDisable(false);
        databaseContent.setDisable(false);
    }

    @FXML
    private void loadDatabase() {
        var eventBus = springContext.getBean(EventBus.class);

        var fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new ExtensionFilter(resourceBundle.getString("main-window.file-chooser.db-file-type"), "*.db"));
        fileChooser.getExtensionFilters().add(new ExtensionFilter(resourceBundle.getString("main-window.file-chooser.all-files"), "*"));

        var databaseFile = fileChooser.showOpenDialog(null);
        eventBus.post(new LoadDatabaseFromFile(databaseFile));
    }

    private DatabaseContent decryptDatabaseContent(String encryptedContent, EncryptionType encryptionType) {
        var dialog = new PasswordInputDialog(resourceBundle.getString("main-window.password-prompt.password"));
        dialog.setTitle(resourceBundle.getString("main-window.password-prompt.title"));

        var password = dialog.showAndWait().orElse(null);
        if (password != null) {
            try {
                return encryptionService.decryptDatabaseContent(encryptedContent, password, encryptionType);
            } catch (Exception e) {
                showDecryptionFailedAlert();
                return decryptDatabaseContent(encryptedContent, encryptionType);
            }
        } else {
            return null;
        }
    }

    private void showDecryptionFailedAlert() {
        var alert = new Alert(Alert.AlertType.WARNING);
        alert.getDialogPane().setContent(new Label(resourceBundle.getString("main-window.password-prompt.decryption-failed")));
        bringAlertTop(alert);
        alert.showAndWait();
    }

    private void bringAlertTop(Alert alert) {
        var stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(mainWindow.getScene().getWindow());
        stage.setAlwaysOnTop(true);
        stage.setOnShown(event -> stage.requestFocus());
    }
}
