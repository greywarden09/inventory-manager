package pl.greywarden.tools.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class DatabaseService {
    private static final String DATABASE_EXTENSION = ".db";

    public String getDatabasePath(String directory, String databaseName) {
        return Paths.get(directory, databaseName + DATABASE_EXTENSION).toString();
    }

    public boolean isValidDirectory(String path) {
        return StringUtils.isNotEmpty(path) && Files.isDirectory(Paths.get(path));
    }
}
