package pl.greywarden.tools.component;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import pl.greywarden.tools.model.IdGenerationStrategy;

import java.util.ResourceBundle;

public class IdGeneratorComboBox extends ComboBox<IdGenerationStrategy> {
    private ResourceBundle resourceBundle;

    public IdGeneratorComboBox() {
        super();
        initComponent();
    }

    private void initComponent() {
        getItems().setAll(IdGenerationStrategy.values());
        setCellFactory(new Callback<>() {
            @Override
            public ListCell<IdGenerationStrategy> call(ListView<IdGenerationStrategy> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(IdGenerationStrategy item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            setText(resourceBundle.getString("create-database.id-generator." + item.name()));
                        }
                    }
                };
            }
        });
        Platform.runLater(() -> getSelectionModel().selectFirst());
    }

    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

}
