package org.backmeup.storage.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.backmeup.storage.model.Metadata;
import org.backmeup.storage.model.StorageUser;

public interface StorageClient {
    String authenticate(String username, String password) throws IOException;

    Metadata saveFile(String accessToken, String path, boolean overwrite, long numBytes, InputStream data) throws IOException;

    void getFile(String accessToken, String path, OutputStream data) throws IOException;

    //TODO still need to be defined
    void addFileAccessRights(StorageUser user, String filePath);

    //TODO still need to be defined
    void removeFileAccessRights(StorageUser user, String filePath);

    /**
     * Takes a given filePath within a given userspace e.g. owner1/file1.xml and checks if a given currUser is allowed
     * to access this encrypted file
     * 
     * @param accessToken
     * @param ownerId
     * @param filePath
     * @param kscurrUserId
     * @param BMUcurrUserId
     * @return
     * @throws IOException
     */
    public boolean hasFileAccessRights(String accessToken, String ownerId, String filePath, String kscurrUserId, Long BMUcurrUserId)
            throws IOException;
}
