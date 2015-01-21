package org.backmeup.storage.model;

import java.util.Date;

public class Metadata {

    private String size;
    private long bytes;
    private String hash;
    private Date modified;
    private String path;
    private boolean isDir;
    
    public Metadata() {
        this.isDir = false;
    }

    public Metadata(long bytes, String hash, Date modified, String path) {
        super();
        this.bytes = bytes;
        this.size = humanReadableByteCount(bytes, true);
        this.hash = hash;
        this.modified = (Date) modified.clone();
        this.path = path;
        this.isDir = false;
    }

    public long getBytes() {
        return bytes;
    }
    
    public void setBytes(long bytes) {
        this.bytes = bytes;
        this.size = humanReadableByteCount(bytes, true);
    }
    
    public String getSize() {
        return size;
    }

    public String getHash() {
        return hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }

    public Date getModified() {
        return (Date) modified.clone();
    }
    
    public void setModified(Date modified) {
        this.modified = (Date) modified.clone();
    }

    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDir() {
        return isDir;
    }

    // This method was taken from: 
    // https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
