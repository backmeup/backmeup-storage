package org.backmeup.storage.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.backmeup.storage.model.Metadata;

public interface StorageClient {
    Metadata saveFile(String accessToken, String path, boolean overwrite, long numBytes, InputStream data) throws IOException;

    void getFile(String accessToken, String path, OutputStream data) throws IOException;
}
