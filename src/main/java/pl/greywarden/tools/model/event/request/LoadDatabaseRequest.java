package pl.greywarden.tools.model.event.request;

import lombok.Data;
import pl.greywarden.tools.model.database.Database;

@Data
public class LoadDatabaseRequest {
    private final Database database;
    private String password;
}
