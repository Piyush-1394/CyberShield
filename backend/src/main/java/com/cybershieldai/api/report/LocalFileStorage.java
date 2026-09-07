package com.cybershieldai.api.report;

import com.cybershieldai.api.config.AppProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class LocalFileStorage implements FileStorage {
    private final Path root;

    public LocalFileStorage(AppProperties props) {
        try {
            this.root = Path.of(props.reportsDir()).toAbsolutePath();
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create reports directory", e);
        }
    }

    @Override
    public Path save(String relativeName, byte[] content) throws IOException {
        Path target = root.resolve(relativeName).normalize();
        if (!target.startsWith(root)) {
            throw new IOException("Invalid path");
        }
        Files.createDirectories(target.getParent());
        Files.write(target, content);
        return target;
    }

    @Override
    public byte[] read(String relativeName) throws IOException {
        Path target = root.resolve(relativeName).normalize();
        if (!target.startsWith(root)) {
            throw new IOException("Invalid path");
        }
        return Files.readAllBytes(target);
    }
}
