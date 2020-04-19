package pl.greywarden.tools.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import pl.greywarden.tools.listener.EventListener;
import pl.greywarden.tools.model.database.Column;
import pl.greywarden.tools.model.database.Database;
import pl.greywarden.tools.model.database.DatabaseContent;
import pl.greywarden.tools.model.event.CreateDatabaseRequest;
import pl.greywarden.tools.model.event.LoadDatabaseFromFile;
import pl.greywarden.tools.model.event.LoadDatabaseRequest;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@EventListener
@RequiredArgsConstructor
public class DatabaseService {
    private static final String DATABASE_EXTENSION = ".db";
    private final ConfigurableApplicationContext springContext;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        var content = new DatabaseContent();
        var data = new ArrayList<Map<String, Object>>();
        var columns = createDatabaseRequest.getColumns().stream().map(mapping -> {
            var name = mapping.getColumnName();
            var type = mapping.getColumnType();
            return new Column(name, type);
        }).collect(Collectors.toList());
        content.setColumns(columns);
        content.setData(data);

        var database = new Database()
                .withPath(path)
                .withEncryption(encryption)
                .withEncryptionType(encryptionType)
                .withDatabaseContent(content);

        objectMapper.writeValue(new BufferedOutputStream(new FileOutputStream(path)), database);
        var eventBus = springContext.getBeanFactory().getBean(EventBus.class);
        eventBus.post(new LoadDatabaseRequest(database));
    }

    @Subscribe
    @SneakyThrows
    public void loadDatabaseFromFile(LoadDatabaseFromFile loadRequest) {
        var database = objectMapper.readValue(loadRequest.getDatabaseFile(), Database.class);
        var eventBus = springContext.getBeanFactory().getBean(EventBus.class);
        eventBus.post(new LoadDatabaseRequest(database));
    }
}
