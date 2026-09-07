package com.cybershieldai.api.report;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorage {
    Path save(String relativeName, byte[] content) throws IOException;

    byte[] read(String relativeName) throws IOException;
}
