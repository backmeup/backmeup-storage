package org.backmeup.storage.service.resources;

import java.util.Date;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.backmeup.service.client.BackmeupService;
import org.backmeup.storage.service.auth.AuthInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authenticate users and manage access tokens.
 */
@Path("/authenticate")
@RequestScoped
public class Authentication {
    private static final Logger LOGGER = LoggerFactory.getLogger(Authentication.class);
    
    @Inject
    private BackmeupService backmeupService;

    public BackmeupService getBackmeupService() {
        return backmeupService;
    }

    @PermitAll
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthInfo authenticate(@QueryParam("username") String username,
            @QueryParam("password") String password) {
        try {
            BackmeupService serviceClient = getBackmeupService();
            org.backmeup.service.client.model.auth.AuthInfo authInfo = serviceClient.authenticate(username, password);
            String accessToken = authInfo.getAccessToken();
            Date expiresAt = authInfo.getExpiresAt();
            return new AuthInfo(accessToken, expiresAt);
        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }
    }
}
