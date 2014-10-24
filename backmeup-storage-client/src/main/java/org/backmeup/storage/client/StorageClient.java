package org.backmeup.storage.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StorageClient {
    void saveFile(String targetPath, boolean overwrite, long numBytes, InputStream data) throws IOException;

    File getFile(String path, OutputStream data) throws IOException;
}
