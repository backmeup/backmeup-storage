package org.backmeup.storage.model;

public class StorageUser {
    private final Long userId;
    private final String authToken;

    public StorageUser(Long userId, String authToken) {
        this.userId = userId;
        this.authToken = authToken;
    }

    public Long getUserId() {
        return userId;
    }
    
    public String getAuthToken() {
        return authToken;
    }

    @Override
    public String toString() {
        return userId.toString();
    }
}
