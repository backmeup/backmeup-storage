package org.backmeup.storage.logic.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.backmeup.storage.logic.StorageLogic;
import org.backmeup.storage.model.Metadata;
import org.backmeup.storage.model.StorageUser;
import org.backmeup.storage.model.utils.StringUtils;
import org.backmeup.storage.service.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@RequestScoped
public class LocalFilesystemStorage implements StorageLogic {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFilesystemStorage.class);
    private static final String BASE_PATH = Configuration.getProperty("backmeup.storage.home");
    private static final String DIGEST_ALGORITHM = "MD5";


    public LocalFilesystemStorage() {

    }

    @Override
    public File getFile(StorageUser user, String path) {
        final String userPath = getUserFilePath(path, user);
        final String completePath = BASE_PATH + userPath;
        final Path filePath = Paths.get(completePath);
        
        return new File(filePath.toAbsolutePath().toString());
    }

    @Override
    public Metadata saveFile(StorageUser user, String filePath, boolean overwrite, long contentLength, InputStream content) {
        final String userFilePath = getUserFilePath(filePath, user);
        final String completePath = BASE_PATH + userFilePath;
        final Path path = Paths.get(completePath);

        File file = new File(path.toAbsolutePath().toString());
        if (file.exists() && !overwrite) {
            throw new WebApplicationException(Status.CONFLICT);
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
        long totalLength = 0;

        File parent = new File(path.getParent().toAbsolutePath().toString());
        if(!parent.mkdirs()) {
            LOGGER.info("Unable to create parent directory " + parent);
            // maybe throw a filenotfoundexception
        };

        if (!file.canWrite()) {
            file.setWritable(true);
        }

        try (OutputStream out = new FileOutputStream(file);) {
            InputStream in = content;
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                md.update(buffer, 0, len);
                totalLength += len;
            }
        } catch (IOException e) {
            LOGGER.error("", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        String hash;
        try {
            hash = StringUtils.getHexString(md.digest());
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("", e);
            hash = "-1";
        }
        return new Metadata(totalLength, hash, new Date(), filePath);
    }
    
    protected String getUserFilePath(String filePath, StorageUser user) {
        return "/" + user.getUserId() + "/" + filePath;
    }
}
