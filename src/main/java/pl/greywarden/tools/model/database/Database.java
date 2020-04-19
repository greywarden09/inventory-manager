package pl.greywarden.tools.model.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import pl.greywarden.tools.EncryptionType;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class Database {
    private String path;
    private boolean encryption;
    private EncryptionType encryptionType;
    private DatabaseContent databaseContent;
}
