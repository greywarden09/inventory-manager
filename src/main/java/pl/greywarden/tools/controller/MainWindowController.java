package pl.greywarden.tools.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import pl.greywarden.tools.component.DatabaseTableView;
import pl.greywarden.tools.component.columns.BooleanTableColumn;
import pl.greywarden.tools.component.columns.IdTableColumn;
import pl.greywarden.tools.listener.EventListener;
import pl.greywarden.tools.model.database.Database;
import pl.greywarden.tools.model.event.LoadDatabaseFromFile;
import pl.greywarden.tools.model.event.LoadDatabaseRequest;

import java.net.URL;
import java.util.ResourceBundle;

@Controller
@EventListener
@RequiredArgsConstructor
public class MainWindowController implements Initializable {
    private final ConfigurableApplicationContext springContext;
    public VBox controlPanel;
    public DatabaseTableView databaseContent;
    private final ObjectProperty<Database> database = new SimpleObjectProperty<>();

    @FXML
    public void exit() {
        Platform.exit();
    }

    public void createNewDatabase() {
        var createDatabaseDialog = springContext.getBean("createDatabaseDialog", Stage.class);
        if (!createDatabaseDialog.isShowing()) {
            createDatabaseDialog.show();
            createDatabaseDialog.requestFocus();
        }
    }

    @Subscribe
    public void loadDatabase(LoadDatabaseRequest loadDatabaseRequest) {
        databaseContent.getItems().clear();
        databaseContent.getColumns().clear();

        var database = loadDatabaseRequest.getDatabase();
        this.database.setValue(database);

        var columns = database.getDatabaseContent().getColumns();
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
        for (var entry: database.getDatabaseContent().getData()) {
            var observable = FXCollections.observableMap(entry);
            databaseContent.getItems().add(observable);
        }

        controlPanel.setDisable(false);
        databaseContent.setDisable(false);
    }

    public void loadDatabase() {
        var eventBus = springContext.getBean(EventBus.class);

        var fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Database", "*.db"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*"));

        var databaseFile = fileChooser.showOpenDialog(null);
        eventBus.post(new LoadDatabaseFromFile(databaseFile));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        databaseContent.setEditable(true);
    }
}
