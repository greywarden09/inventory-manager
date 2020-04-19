package pl.greywarden.tools.model.event;

import lombok.Data;
import pl.greywarden.tools.model.database.Database;

@Data
public class LoadDatabaseRequest {
    private final Database database;
}
