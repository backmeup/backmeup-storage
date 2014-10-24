package org.backmeup.storage.service.auth;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import org.backmeup.storage.model.StorageUser;

public class StorageSecurityContext implements SecurityContext{
    private final UserPrincipal user;

    public StorageSecurityContext(StorageUser user) {
        this.user = new UserPrincipal(user.getUserId().toString(), user);
    }

    @Override
    public Principal getUserPrincipal() {
        return user;
    }

    @Override
    public boolean isUserInRole(String role) {
        return true;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return null;
    }

}
