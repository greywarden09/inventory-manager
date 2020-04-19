package pl.greywarden.tools.model.database;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DatabaseContent {
    private List<Column> columns;
    private List<Map<String, Object>> data;
}
