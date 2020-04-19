package pl.greywarden.tools.model.event;

import lombok.Data;

import java.io.File;

@Data
public class LoadDatabaseFromFile {
    private final File databaseFile;
}
