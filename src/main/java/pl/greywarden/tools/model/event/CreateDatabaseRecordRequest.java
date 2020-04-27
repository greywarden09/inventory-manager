package pl.greywarden.tools.model.event;

import javafx.collections.ObservableMap;
import lombok.Data;

@Data
public class CreateDatabaseRecordRequest {
    private final ObservableMap<String, Object> databaseRecord;
}
