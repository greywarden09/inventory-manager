package pl.greywarden.tools.model.event.request;

import lombok.Data;
import pl.greywarden.tools.model.database.Database;

@Data
public class SaveDatabaseRequest {
    private final Database database;
}
