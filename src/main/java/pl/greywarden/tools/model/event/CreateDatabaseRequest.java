package pl.greywarden.tools.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import pl.greywarden.tools.model.EncryptionType;
import pl.greywarden.tools.model.InventoryItemColumn;

import java.util.List;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class CreateDatabaseRequest {
    private String databasePath;
    private String encryptionPassword;
    private List<InventoryItemColumn> columns;
    private boolean encryption;
    private EncryptionType encryptionType;
}
