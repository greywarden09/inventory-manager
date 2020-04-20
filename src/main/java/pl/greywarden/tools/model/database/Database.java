package pl.greywarden.tools.model.database;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String encryptionPassword;
    private EncryptionType encryptionType;
    private Object databaseContent;
    //private String encryptedContent;

    public <T> T getDatabaseContent() {
        return (T) databaseContent;
    }
}
