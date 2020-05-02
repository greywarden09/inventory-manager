package pl.greywarden.tools.model.event.response;

import javafx.collections.ObservableMap;
import lombok.Data;

@Data
public class CreateDatabaseRecord {
    private final ObservableMap<String, Object> databaseRecord;
}
