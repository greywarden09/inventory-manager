package pl.greywarden.tools.service;

import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class FileService {
    public boolean hasReadWritePermission(String path) {
        File file = new File(path);
        return file.canRead() && file.canWrite();
    }

    public boolean exists(String path) {
        File file = new File(path);
        return file.exists() && file.isFile();
    }
}
