package pl.greywarden.tools.model.event.request;

import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import lombok.Data;
import pl.greywarden.tools.model.database.Column;

import java.util.List;

@Data
public class CreateDatabaseRecordRequest {
    private final List<Column> columns;
    private final ObservableList<ObservableMap<String, Object>> database;
}
