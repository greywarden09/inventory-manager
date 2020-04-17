package pl.greywarden.tools.component;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import pl.greywarden.tools.EncryptionType;

public class EncryptionTypeComboBox extends ComboBox<EncryptionType> {
    public EncryptionTypeComboBox() {
        super();
        initComponent();
    }

    private void initComponent() {
        getItems().setAll(EncryptionType.values());
        setCellFactory(new Callback<>() {
            @Override
            public ListCell<EncryptionType> call(ListView<EncryptionType> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(EncryptionType item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            }
        });
        getSelectionModel().selectFirst();
    }
}
