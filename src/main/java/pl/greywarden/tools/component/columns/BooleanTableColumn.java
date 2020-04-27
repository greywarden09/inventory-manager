package pl.greywarden.tools.component.columns;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;

public class BooleanTableColumn extends TableColumn<ObservableMap<String, Object>, Boolean> {
    public BooleanTableColumn(String name) {
        super(name);
        super.setCellValueFactory(param -> new SimpleBooleanProperty(Boolean.parseBoolean(param.getValue().get(name).toString())));
        super.setCellFactory(tc -> {
            var checkBox = new CheckBox();
            var tableCell = new TableCell<ObservableMap<String, Object>, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    if (empty) {
                        setGraphic(null);
                    } else {
                        checkBox.setSelected(item);
                        setGraphic(checkBox);
                    }
                }
            };
            checkBox.setOnAction(event -> {
                var table = tableCell.getTableView();
                var tableRow = tableCell.getTableRow();
                var tableColumn = tableCell.getTableColumn();
                var tablePosition = new TablePosition<>(table, tableRow.getIndex(), tableColumn);

                tableRow.getItem().put(name, checkBox.isSelected());

                var editEvent = new TableColumn.CellEditEvent<>(
                        table, tablePosition, editCommitEvent(), checkBox.isSelected()
                );
                Event.fireEvent(tableColumn, editEvent);
            });
            tableCell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            tableCell.setAlignment(Pos.CENTER);
            return tableCell;
        });
    }
}
