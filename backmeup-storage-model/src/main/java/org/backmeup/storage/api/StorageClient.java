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
    void addFileAccessRights(String accessToken, String ownerId, String filePath,String kscurrUserId, Long BMUcurrUserId);

    //TODO still need to be defined
    void removeFileAccessRights(StorageUser user, String filePath);

    /**
     * Takes a given filePath within a given userspace e.g. owner1/file1.xml and checks if a given currUser is allowed
     * to access this encrypted file
     * 
     * @param accessToken access token of the current user
     * @param ownerId the userspace prefix to look up the file for
     * @param filePath the file path withi a given userspace
     * @param kscurrUserId keyserverUserId of the current user
     * @param BMUcurrUserId backmeupUserId of the current user
     * @param checkUserId userId to check access rights for - if null access rights for the current user will be checked
     * @return
     * @throws IOException
     */
    public boolean hasFileAccessRights(String currUseraccessToken, String ownerId, String filePath, String kscurrUserId, Long BMUcurrUserId, 
            Long checkUserId) throws IOException;
}
