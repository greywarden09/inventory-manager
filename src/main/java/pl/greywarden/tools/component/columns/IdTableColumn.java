package pl.greywarden.tools.component.columns;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

public class IdTableColumn extends TableColumn<ObservableMap<String, Object>, String> {
    public IdTableColumn(String name) {
        super(name);
        super.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(name).toString()));
        super.setCellFactory(tc -> new IdColumnTableCell());
        super.setOnEditCommit(event -> {
            var editedItem = event.getRowValue();
            editedItem.put(name, event.getNewValue());
        });
    }

    public static class IdColumnTableCell extends TextFieldTableCell<ObservableMap<String, Object>, String> {
        public IdColumnTableCell() {
            super(new DefaultStringConverter());
        }

        @Override
        public void commitEdit(String newValue) {
            var table = getTableView();
            var column = getTableColumn();
            var oldValue = getItem();
            if (newValue.equals(oldValue)) {
                super.cancelEdit();
            } else {
                var columnName = column.getText();
                var uniqueId = table.getItems()
                        .stream()
                        .map(item -> item.get(columnName).toString())
                        .filter(id -> id.equals(newValue))
                        .findAny().isEmpty();
                if (uniqueId) {
                    getStyleClass().remove("error");
                    setItem(newValue);
                    super.commitEdit(newValue);
                } else {
                    var styleClass = getStyleClass();
                    if (!styleClass.contains("error")) {
                        styleClass.add("error");
                    }
                }
            }
        }

    }
}
