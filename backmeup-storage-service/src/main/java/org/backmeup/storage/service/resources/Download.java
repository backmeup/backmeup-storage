package org.backmeup.storage.service.resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

import org.backmeup.keyserver.client.KeyserverClient;
import org.backmeup.keyserver.model.KeyserverException;
import org.backmeup.keyserver.model.Token.Kind;
import org.backmeup.keyserver.model.dto.AuthResponseDTO;
import org.backmeup.keyserver.model.dto.TokenDTO;
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
    
    @Inject
    private KeyserverClient keyserverClient;
    
    public StorageLogic getStorageLogic() {
        return storageLogic;
    }

    
    @PermitAll
    @GET
    @Path("/{accessToken:[^/]+}/{filePath:.+}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@PathParam("accessToken") String accessToken, @PathParam("filePath") String filePath) {
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
                    .build();
                    //.header("Content-Disposition",
                    //        "attachment; filename=" + file.getName()).build();
        } catch (IOException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }
    
    protected StorageUser getUserFromAccessToken(String accessToken) {
        if ("".equals(accessToken)) {
            throw new WebApplicationException(ACCESS_DENIED);
        }
        
        try {
            TokenDTO token = new TokenDTO(Kind.INTERNAL, accessToken);
            AuthResponseDTO response = keyserverClient.authenticateWithInternalToken(token);
            String userId = response.getUsername();

            return new StorageUser(Long.parseLong(userId));
        } catch (KeyserverException ke) {
            throw new WebApplicationException(ACCESS_DENIED);
        }
    }
}
