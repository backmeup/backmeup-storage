package org.backmeup.storage.logic;

import java.io.File;
import java.io.InputStream;

import org.backmeup.storage.model.dto.Metadata;

public interface StorageLogic {
	File getFile(String path);
	
	Metadata saveFile(String filePath, boolean overwrite, long contentLength, InputStream content);
}
