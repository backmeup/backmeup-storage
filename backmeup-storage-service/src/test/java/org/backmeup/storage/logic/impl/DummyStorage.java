package org.backmeup.storage.logic.impl;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.backmeup.storage.logic.StorageLogic;
import org.backmeup.storage.model.dto.Metadata;

public class DummyStorage implements StorageLogic {
    private final Map<String, File> files;

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
    public File getFile(String path) {
        return files.get(path);
    }

    @Override
    public Metadata saveFile(String filePath, boolean overwrite, long contentLength, InputStream content) {
        File file = getFile("should return the only file for any key");
        return new Metadata(file.length(), "900150983cd24fb0d6963f7d28e17f72", new Date(), file.getPath());
    }

}
