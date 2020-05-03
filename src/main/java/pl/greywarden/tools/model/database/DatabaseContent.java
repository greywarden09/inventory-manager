package pl.greywarden.tools.model.database;

import lombok.Data;
import pl.greywarden.tools.model.IdGenerationStrategy;

import java.util.List;
import java.util.Map;

@Data
public class DatabaseContent {
    private List<Column> columns;
    private List<Map<String, Object>> data;
    private IdGenerationStrategy idGenerationStrategy;
}
