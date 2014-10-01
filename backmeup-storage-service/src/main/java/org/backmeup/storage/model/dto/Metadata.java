package org.backmeup.storage.model.dto;

import java.util.Date;

public class Metadata {
	private final String size; // A human-readable string version of the file size
	private final long bytes;
	private final String hash;
	private final Date modified;
	private final String path;
	private final boolean isDir;
//	private final List<Metadata> children;
	
	public Metadata(long bytes, String hash, Date modified, String path) {
		super();
		this.bytes = bytes;
		this.size = humanReadableByteCount(bytes, true);
		this.hash = hash;
		this.modified = modified;
		this.path = path;
		this.isDir = false;
//		this.children = null;
	}
	
	public String getSize() {
		return size;
	}

	public long getBytes() {
		return bytes;
	}

	public String getHash() {
		return hash;
	}

	public Date getModified() {
		return modified;
	}

	public String getPath() {
		return path;
	}

	public boolean isDir() {
		return isDir;
	}

	// This method was taken from: 
	// https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
	private static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "KMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
