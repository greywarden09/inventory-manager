package pl.greywarden.tools.component.columns;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.BigDecimalStringConverter;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;

public class NumberTableColumn extends TableColumn<ObservableMap<String, Object>, BigDecimal> {
    public NumberTableColumn(String name) {
        super(name);
        super.setCellValueFactory(param -> new SimpleObjectProperty<>(NumberUtils.createBigDecimal(param.getValue().get(name).toString())));
        super.setCellFactory(tc -> new NumberColumnTableCell());
        super.setOnEditCommit(event -> {
            var editedItem = event.getRowValue();
            editedItem.put(name, event.getNewValue());
        });
    }

    public static class NumberColumnTableCell extends TextFieldTableCell<ObservableMap<String, Object>, BigDecimal> {
        public NumberColumnTableCell() {
            super(new BigDecimalStringConverter());
        }
    }
}
