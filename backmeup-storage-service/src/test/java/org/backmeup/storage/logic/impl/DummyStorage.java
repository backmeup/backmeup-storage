package org.backmeup.storage.logic.impl;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

import org.backmeup.storage.logic.StorageLogic;
import org.backmeup.storage.model.dto.Metadata;

public class DummyStorage implements StorageLogic {
	private final File file;
	
	public DummyStorage(File file) {
		this.file = file;
	}

	@Override
	public File getFile(String path) {
		return file;
	}

	@Override
	public Metadata saveFile(String filePath, boolean overwrite,
			long contentLength, InputStream content) {
		return new Metadata(file.length(), "900150983cd24fb0d6963f7d28e17f72", new Date(), file.getPath());
	}

}
