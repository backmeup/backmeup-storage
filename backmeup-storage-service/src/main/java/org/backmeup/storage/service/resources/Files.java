package org.backmeup.storage.service.resources;

import java.io.File;
import java.io.IOException;

import javax.annotation.security.RolesAllowed;
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

import org.backmeup.storage.logic.StorageLogic;
import org.backmeup.storage.model.Metadata;
import org.backmeup.storage.service.auth.AuthRoles;

@Path("/files")
public class Files {

    @Inject
    private StorageLogic storageLogic;

    public StorageLogic getStorageLogic() {
        return storageLogic;
    }

    @RolesAllowed(AuthRoles.USER)
    @GET
    @Path("/{path:[^/]+.*}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@PathParam("path") String filePath) {
        File file = getStorageLogic().getFile(filePath);
        if (file == null || !file.exists()) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        return Response
                .ok(file)
                .header("Content-Disposition",
                        "attachment; filename=" + file.getName()).build();
    }

    @RolesAllowed(AuthRoles.USER)
    @PUT
    @Path("/{path:[^/]+.*}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putFile(@Context HttpServletRequest request, 
            @PathParam("path") String filePath, 
            @QueryParam("overwrite") @DefaultValue("false") boolean overwrite,
            @HeaderParam("Content-Length") long contentLength) throws IOException {
        if (contentLength <= 0) {
            throw new WebApplicationException(Status.LENGTH_REQUIRED);
        }

        Metadata fileMetadata = getStorageLogic().saveFile(filePath, overwrite, contentLength, request.getInputStream());
        return Response.ok(fileMetadata).build();
    }
}
