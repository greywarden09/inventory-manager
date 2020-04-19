package pl.greywarden.tools.model;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import pl.greywarden.tools.model.database.ColumnType;

public class InventoryItemColumn {
    private final SimpleStringProperty columnName = new SimpleStringProperty();
    private final SimpleObjectProperty<ColumnType> columnType = new SimpleObjectProperty<>();

    public InventoryItemColumn(String columnName, ColumnType columnType) {
        this.columnName.set(columnName);
        this.columnType.set(columnType);
    }

    public String getColumnName() {
        return columnName.get();
    }

    public void setColumnName(String columnName) {
        this.columnName.set(columnName);
    }

    public ColumnType getColumnType() {
        return columnType.get();
    }

    public void setColumnType(ColumnType columnType) {
        this.columnType.set(columnType);
    }
}
