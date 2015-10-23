package org.backmeup.storage.logic;

import java.io.File;
import java.io.InputStream;

import org.backmeup.storage.model.Metadata;
import org.backmeup.storage.model.StorageUser;

public interface StorageLogic {
    File getFile(StorageUser user, String path);

    File getFile(StorageUser user, String owner, String path);

    InputStream getFileAsInputStream(StorageUser user, String owner, String path);

    Metadata saveFile(StorageUser user, String filePath, boolean overwrite, long contentLength, InputStream content);

    //TODO still needs to be defined
    void addFileAccessRights(StorageUser user, String filePath);

    //TODO still needs to be defined
    void removeFileAccessRights(StorageUser user, String filePath);

    boolean hasFileAccessRights(StorageUser user, String owner, String filePath);
}
