package org.backmeup.storage.model;

public class StorageUser {
    private Long userId;

    public StorageUser(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return userId.toString();
    }
}
