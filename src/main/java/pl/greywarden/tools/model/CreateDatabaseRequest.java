package pl.greywarden.tools.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import pl.greywarden.tools.EncryptionType;

import java.util.List;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class CreateDatabaseRequest {
    private String databasePath;
    private List<InventoryItemColumn> columns;
    private boolean encryption;
    private EncryptionType encryptionType;
}
