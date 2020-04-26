package pl.greywarden.tools.model.database;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import pl.greywarden.tools.EncryptionType;
import pl.greywarden.tools.deserializer.DatabaseDeserializer;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(using = DatabaseDeserializer.class)
public class Database {
    private String path;
    private boolean encryption;
    private String encryptionPassword;
    private EncryptionType encryptionType;
    private Object databaseContent;

    @SuppressWarnings("unchecked")
    public <T> T getDatabaseContent() {
        return (T) databaseContent;
    }
}
