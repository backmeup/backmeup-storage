package org.backmeup.storage.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.StringTokenizer;

import org.backmeup.storage.client.config.Configuration;
import org.backmeup.storage.model.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackmeupStorageClientDummy /*implements StorageClient*/ {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackmeupStorageClientDummy.class);

    private String accessToken;
    private String userId;

    public BackmeupStorageClientDummy(String accessToken) {
        LOGGER.info("Creating Client");
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        this.userId = extractUserIdFromToken(accessToken);
    }

    /**
     * Upload file data to the Backmeup-Storage
     */
    public Metadata saveFile (String targetPath, boolean overwrite, long numBytes, InputStream data) throws IOException {
        if (data != null) {
            String filePath = getStorageHome() + userId + targetPath;
            if (!overwrite) {
                Files.copy(data, Paths.get(filePath));
            } else {
                // overwrite == true
                Files.copy(data, Paths.get(filePath),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return null;
    }

    /**
     * Retrieves a file's data and writes it to the the given OutputStream
     */
    public Metadata getFile (String path, OutputStream data) throws IOException {
        String filePath = getStorageHome() + userId + path;
        File file = new File(filePath);
        if (file.exists() && file.canRead()) {
            Files.newOutputStream(Paths.get(filePath), StandardOpenOption.READ);
//            return file;
            return null;
        }
        throw new IOException("File not found or accessible: " + filePath);
    }

    public void copy(String fromPath, String toPath) {
        throw new UnsupportedOperationException();
    }

    public void move(String fromPath, String toPath) {
        throw new UnsupportedOperationException();
    }

    public void delete(String path) {
        throw new UnsupportedOperationException();
    }

    private static String getStorageHome() {
        String homePath = Configuration.getProperty("backmeup.storage.home");
        if (homePath != null && homePath.length() > 0 && !homePath.contains("\"")) {
            File f = new File(homePath);
            if (!f.isDirectory()) {
                throw new ExceptionInInitializerError("User home must point to a directory: " + f);
            }
            if (!f.exists()) {
                if (!f.mkdirs()) {
                    throw new ExceptionInInitializerError("Can not create directory: " + f);
                }
            }
            return f.getAbsolutePath();
        }
        throw new ExceptionInInitializerError("User Home dir not properly configured within backmeup-storage-client.properties");
    }

    private String extractUserIdFromToken(String token) {
        // Token has the form "userid;password"
        final StringTokenizer tokenizer = new StringTokenizer(token, ";");
        return tokenizer.nextToken(); 
    }
}
