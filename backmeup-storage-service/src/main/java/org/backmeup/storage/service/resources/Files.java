package org.backmeup.storage.service.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.backmeup.keyserver.client.KeyserverClient;
import org.backmeup.keyserver.model.KeyserverException;
import org.backmeup.keyserver.model.dto.AuthResponseDTO;
import org.backmeup.keyserver.model.dto.TokenDTO;
import org.backmeup.storage.logic.StorageLogic;
import org.backmeup.storage.model.Metadata;
import org.backmeup.storage.model.StorageUser;
import org.backmeup.storage.service.auth.AuthRoles;
import org.backmeup.storage.service.auth.UserPrincipal;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ServerResponse;

@Path("/files")
@RequestScoped
public class Files {

    private static final ServerResponse ACCESS_DENIED = new ServerResponse("Access denied for this resource", 401, new Headers<>());

    @Inject
    private StorageLogic storageLogic;
    @Inject
    private KeyserverClient keyserverClient;

    public StorageLogic getStorageLogic() {
        return this.storageLogic;
    }

    @RolesAllowed(AuthRoles.USER)
    @GET
    @Path("/{path:[^/]+.*}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(//
            @PathParam("path") String filePath, //
            @QueryParam("owner") String owner, //
            @Context SecurityContext securityContext) {
        StorageUser user = getUserFromContext(securityContext);

        String ownerId = owner;
        if (ownerId == null || ownerId.isEmpty()) {
            ownerId = user.getUserId().toString();
        }

        InputStream file = getStorageLogic().getFileAsInputStream(user, ownerId, filePath);
        if (file == null) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        java.nio.file.Path p = Paths.get(filePath);
        return Response.ok(file).header("Content-Disposition", "attachment; filename=" + p.getFileName().toString()).build();
    }

    @RolesAllowed(AuthRoles.USER)
    @PUT
    @Path("/{path:[^/]+.*}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putFile(//
            @PathParam("path") String filePath, //
            @QueryParam("overwrite") @DefaultValue("false") boolean overwrite,//
            @HeaderParam("Content-Length") long contentLength, //
            @Context HttpServletRequest request, //
            @Context SecurityContext securityContext)//
            throws IOException {
        if (contentLength <= 0) {
            throw new WebApplicationException(Status.LENGTH_REQUIRED);
        }

        StorageUser user = getUserFromContext(securityContext);

        Metadata fileMetadata = getStorageLogic().saveFile(user, filePath, overwrite, contentLength, request.getInputStream());
        return Response.ok(fileMetadata).build();
    }

    /*@RolesAllowed(AuthRoles.USER)
    @PUT
    @Path("/rights/{path:[^/]+.*}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addFileAccessRights(//
            @PathParam("path") String filePath, //
            @Context HttpServletRequest request,//
            @Context SecurityContext securityContext)//
            throws IOException {

        StorageUser user = getUserFromContext(securityContext);

        //TODO CONTINUE HERE
    }*/

    /* @RolesAllowed(AuthRoles.USER)
     @DELETE
     @Path("/rights/{path:[^/]+.*}")
     @Consumes(MediaType.APPLICATION_OCTET_STREAM)
     @Produces(MediaType.APPLICATION_JSON)
     public Response removeFileAccessRights(//
             @PathParam("path") String filePath, //
             @Context HttpServletRequest request,//
             @Context SecurityContext securityContext)//
             throws IOException {

         StorageUser user = getUserFromContext(securityContext);

         //TODO CONTINUE HERE
     }*/

    @PermitAll
    @GET
    @Path("/rights/{owner:[^/]+}/{path:[^/]+.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response hasFileAccessRights(//
            @PathParam("path") String filePath, //
            @PathParam("owner") String owner,//
            @QueryParam("accesstoken") String accessToken, //
            @QueryParam("ksuserid") String currUserKSuserid,//
            @QueryParam("bmuuserid") Long currUserBMUuserid)//
            throws IOException {
        mandatory("path", filePath);
        mandatory("owner", owner);
        mandatory("accesstoken", accessToken);
        mandatory("ksuserid", currUserKSuserid);
        mandatory("bmuuserid", currUserBMUuserid);

        StorageUser currUser = getUserFromAccessToken(accessToken, currUserKSuserid, currUserBMUuserid);

        Boolean access = getStorageLogic().hasFileAccessRights(currUser, owner, filePath);
        return Response.ok(access).build();
    }

    protected StorageUser getUserFromContext(SecurityContext context) {
        StorageUser activeUser = ((UserPrincipal) context.getUserPrincipal()).getUser();
        if (activeUser.getUserId() == null) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        return activeUser;
    }

    private void mandatory(String name, String v) {
        if (v == null || v.isEmpty()) {
            badRequestMissingParameter(name);
        }
    }

    private void mandatory(String name, Long l) {
        if (l == null || l == 0) {
            badRequestMissingParameter(name);
        }
    }

    private void badRequestMissingParameter(String name) {
        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST). //
                entity(name + " parameter is mandatory"). //
                build());
    }

    /**
     * Validates a given access token and compares if the provided keyserverId matches the validated one. This step is
     * required as the keyserver does not hold the bmuUserId for anonymous user accounts
     * 
     * @param accessToken
     * @param ksUserId
     * @param bmuUserId
     * @return
     */
    protected StorageUser getUserFromAccessToken(String accessToken, String ksUserId, Long bmuUserId) {
        if ("".equals(accessToken)) {
            throw new WebApplicationException(ACCESS_DENIED);
        }
        try {
            TokenDTO token = TokenDTO.fromTokenString(accessToken);
            AuthResponseDTO response = this.keyserverClient.authenticateWithInternalToken(token);
            String ksId = response.getServiceUserId();
            if (!ksId.equals(ksUserId)) {
                throw new WebApplicationException(ACCESS_DENIED);
            }
            return new StorageUser(bmuUserId, accessToken);
        } catch (KeyserverException ke) {
            throw new WebApplicationException(ACCESS_DENIED);
        }
    }

}
