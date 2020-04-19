package pl.greywarden.tools.component.columns;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;

public class BooleanTableColumn extends TableColumn<ObservableMap<String, Object>, Boolean> {
    public BooleanTableColumn(String name) {
        super(name);
        super.setCellValueFactory(param -> new SimpleBooleanProperty(Boolean.parseBoolean(param.getValue().get(name).toString())));
        super.setCellFactory(tc -> new CheckBoxTableCell<>());
        super.setOnEditCommit(event -> {
            var editedItem = event.getRowValue();
            editedItem.put(name, event.getNewValue());
        });
    }
}
