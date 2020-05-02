package pl.greywarden.tools.model.event.request;

import lombok.Data;

import java.io.File;

@Data
public class LoadDatabaseFromFileRequest {
    private final File databaseFile;
}
