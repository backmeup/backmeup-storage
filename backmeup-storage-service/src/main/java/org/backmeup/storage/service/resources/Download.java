package org.backmeup.storage.service.resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.StringTokenizer;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.backmeup.storage.logic.StorageLogic;
import org.backmeup.storage.model.StorageUser;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ServerResponse;

@Path("/download")
@RequestScoped
public class Download {
    private static final ServerResponse ACCESS_DENIED = new ServerResponse("Access denied for this resource", 401, new Headers<>());
    
    @Inject
    private StorageLogic storageLogic;
    
    public StorageLogic getStorageLogic() {
        return storageLogic;
    }

    
    @PermitAll
    @GET
    @Path("/{pathWithToken:[^/]+.*}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@PathParam("pathWithToken") String pathWithToken) {
        final StringTokenizer tokenizer = new StringTokenizer(pathWithToken, "&");
        final String filePath = tokenizer.nextToken();
        final String accessToken = tokenizer.nextToken();
        
        StorageUser user = getUserFromAccessToken(accessToken);

        File file = getStorageLogic().getFile(user, filePath);
        if (file == null || !file.exists()) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        try {
            String mediaType = Files.probeContentType(file.toPath());

            if(mediaType == null){
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            return Response
                    .ok(file)
                    .type(mediaType)
                    .header("Content-Disposition",
                            "attachment; filename=" + file.getName()).build();
        } catch (IOException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }
    
    protected StorageUser getUserFromAccessToken(String accessToken) {
        if ("".equals(accessToken)) {
            throw new WebApplicationException(ACCESS_DENIED);
        }
        if (!accessToken.startsWith("accessToken=")) {
            throw new WebApplicationException(ACCESS_DENIED);
        }
        
        String accessTokenValue = accessToken.substring(12);
        final StringTokenizer tokenizer = new StringTokenizer(accessTokenValue, ";");
        final String userId = tokenizer.nextToken();
        return new StorageUser(Long.parseLong(userId));
    }
}
