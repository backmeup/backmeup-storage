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

/**
 * Authenticate users and manage access tokens.
 */
@Path("/authenticate")
@RequestScoped
public class Authentication {
    
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
            Date issueDate = authInfo.getIssueDate();
            return new AuthInfo(accessToken, issueDate);
        } catch (Exception ex) {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }
    }
}
