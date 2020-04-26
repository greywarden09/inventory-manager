package pl.greywarden.tools.component.columns;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

public class TextTableColumn extends TableColumn<ObservableMap<String, Object>, String> {
    public TextTableColumn(String name) {
        super(name);
        super.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().get(name).toString()));
        super.setCellFactory(tc -> new TextColumnTableCell());
        super.setOnEditCommit(event -> {
            var editedItem = event.getRowValue();
            editedItem.put(name, event.getNewValue());
        });
    }

    public static class TextColumnTableCell extends TextFieldTableCell<ObservableMap<String, Object>, String> {
        public TextColumnTableCell() {
            super(new DefaultStringConverter());
        }

        @Override
        public void commitEdit(String newValue) {
            var oldValue = getItem();
            if (oldValue.equals(newValue)) {
                super.cancelEdit();
            } else {
                super.commitEdit(newValue);
            }
        }
    }
}
