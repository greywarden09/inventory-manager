package pl.greywarden.tools.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import pl.greywarden.tools.listener.EventListener;
import pl.greywarden.tools.model.database.Column;
import pl.greywarden.tools.model.database.ColumnType;
import pl.greywarden.tools.model.database.Database;
import pl.greywarden.tools.model.database.DatabaseContent;
import pl.greywarden.tools.model.event.request.CreateDatabaseRequest;
import pl.greywarden.tools.model.event.request.LoadDatabaseFromFileRequest;
import pl.greywarden.tools.model.event.request.LoadDatabaseRequest;
import pl.greywarden.tools.model.event.request.SaveDatabaseRequest;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@EventListener
@RequiredArgsConstructor
public class DatabaseService {
    private static final String DATABASE_EXTENSION = ".db";
    private final ConfigurableApplicationContext springContext;
    private final ObjectMapper objectMapper;

    public String getDatabasePath(String directory, String databaseName) {
        return Paths.get(directory, databaseName + DATABASE_EXTENSION).toString();
    }

    public boolean isValidDirectory(String path) {
        return StringUtils.isNotEmpty(path) && Files.isDirectory(Paths.get(path));
    }

    @Subscribe
    @SneakyThrows
    public void createDatabase(CreateDatabaseRequest createDatabaseRequest) {
        var path = createDatabaseRequest.getDatabasePath();
        var encryption = createDatabaseRequest.isEncryption();
        var encryptionType = createDatabaseRequest.getEncryptionType();
        var encryptionPassword = createDatabaseRequest.getEncryptionPassword();
        var columns = createDatabaseRequest.getColumns().stream().map(mapping -> {
            var name = mapping.getColumnName();
            var type = mapping.getColumnType();
            return new Column(name, type);
        }).collect(Collectors.toList());
        var content = new DatabaseContent();
        var data = new ArrayList<Map<String, Object>>();
        var idGenerationStrategy = createDatabaseRequest.getIdGenerationStrategy();

        content.setColumns(columns);
        content.setData(data);
        content.setIdGenerationStrategy(idGenerationStrategy);

        var database = new Database()
                .withPath(path)
                .withEncryption(encryption)
                .withEncryptionType(encryptionType)
                .withEncryptionPassword(encryptionPassword)
                .withDatabaseContent(content);

        objectMapper.writeValue(new BufferedOutputStream(new FileOutputStream(path)), database);
        var eventBus = springContext.getBeanFactory().getBean(EventBus.class);
        var loadDatabaseRequest = new LoadDatabaseRequest(database);
        loadDatabaseRequest.setPassword(encryptionPassword);
        eventBus.post(loadDatabaseRequest);
    }

    @Subscribe
    @SneakyThrows
    public void loadDatabaseFromFile(LoadDatabaseFromFileRequest loadRequest) {
        var database = objectMapper.readValue(loadRequest.getDatabaseFile(), Database.class);
        var eventBus = springContext.getBeanFactory().getBean(EventBus.class);
        eventBus.post(new LoadDatabaseRequest(database));
    }

    @Subscribe
    @SneakyThrows
    public void saveDatabase(SaveDatabaseRequest request) {
        var database = request.getDatabase();
        var path = database.getPath();

        objectMapper.writeValue(new BufferedOutputStream(new FileOutputStream(path)), database);
    }

    public Optional<Map<String, Object>> findById(DatabaseContent databaseContent, String id) {
        return databaseContent.getColumns()
                .stream()
                .filter(column -> ColumnType.ID.equals(column.getType()))
                .findFirst()
                .map(Column::getName)
                .map(columnName -> databaseContent.getData()
                        .stream()
                        .filter(entry -> entry.get(columnName).equals(id))
                        .findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public List<Map<String, Object>> findByValue(ObservableList<ObservableMap<String, Object>> database, String columnName, String value) {
        return database.stream()
                .filter(entry -> entry.get(columnName).equals(value))
                .collect(Collectors.toList());
    }
}
