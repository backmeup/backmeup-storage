package org.backmeup.storage.logic.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.backmeup.storage.logic.StorageLogic;
import org.backmeup.storage.model.Metadata;
import org.backmeup.storage.model.StorageUser;

public class DummyStorage implements StorageLogic {
    private final Map<String, File> files;

    @SuppressWarnings("serial")
    public DummyStorage(final File file) {
        this(new HashMap<String, File>() {
            @Override
            public File get(@SuppressWarnings("unused") Object key) {
                return file;
            }
        });
    }

    public DummyStorage(Map<String, File> files) {
        this.files = files;
    }

    @Override
    public File getFile(StorageUser user, String path) {
        return files.get(path);
    }
    
    @Override
    public File getFile(StorageUser user, String owner, String path) {
        return files.get(path);
    }
    
    @Override
    public InputStream getFileAsInputStream(StorageUser user, String owner, String path) {
        try {
            return new FileInputStream(files.get(path));
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }

    @Override
    public Metadata saveFile(StorageUser user, String filePath, boolean overwrite, long contentLength, InputStream content) {
        File file = getFile(user, "should return the only file for any key");
        return new Metadata(file.length(), "900150983cd24fb0d6963f7d28e17f72", new Date(), file.getPath());
    }

}
