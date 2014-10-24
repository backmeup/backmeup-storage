package org.backmeup.storage.service.auth;

import java.security.Principal;

import org.backmeup.storage.model.StorageUser;

public class UserPrincipal implements Principal {
    private String userId;
    private final StorageUser user;

    public UserPrincipal(String userId, StorageUser user) {
        super();
        this.userId = userId;
        this.user = user;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public StorageUser getUser() {
        return user;
    }

    @Override
    public String getName() {
        return userId;
    }
}
