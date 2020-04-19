package pl.greywarden.tools.model.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Column {
    private String name;
    private ColumnType type;
}
