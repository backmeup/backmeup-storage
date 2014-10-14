package org.backmeup.storage.logic.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.backmeup.storage.logic.StorageLogic;
import org.backmeup.storage.model.dto.Metadata;
import org.backmeup.storage.model.utils.StringUtils;
import org.backmeup.storage.service.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class LocalFilesystemStorage implements StorageLogic {
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalFilesystemStorage.class);
	private static final String BASE_PATH = Configuration.getProperty("backmeup.storage.home");
	private static final String DIGEST_ALGORITHM = "MD5";
	
	
	public LocalFilesystemStorage() {
		
    }

	@Override
	public File getFile(String path) {
		return new File(BASE_PATH + path);
	}

	@Override
	public Metadata saveFile(String filePath, boolean overwrite, long contentLength, InputStream content) {
		File file = new File(BASE_PATH + filePath);
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
		Metadata fileMetadata = new Metadata(totalLength, hash, new Date(), filePath);
		return fileMetadata;
	}
}
