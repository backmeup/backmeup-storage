package org.backmeup.storage.service.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        return this.storageLogic;
    }

    @PermitAll
    @GET
    @Path("/{accessToken:[^/]+}/{owner:[^/]+}/{filePath:.+}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(//
            @PathParam("accessToken") String accessToken, //
            @PathParam("owner") String owner,//
            @PathParam("filePath") String filePath) {
        StorageUser user = getUserFromAccessToken(accessToken);

        if (owner == null || owner.isEmpty()) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        InputStream file = getStorageLogic().getFileAsInputStream(user, owner, filePath);
        if (file == null) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        java.nio.file.Path p = Paths.get(filePath);
        try {
            String mediaType = Files.probeContentType(p);

            if (mediaType == null) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            return Response//
                    .ok(file)//
                    .type(mediaType)//
                    .build();
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
            AuthResponseDTO response = this.keyserverClient.authenticateWithInternalToken(token);
            //TODO fix workaround; anonymous accounts don't have the BMU User ID stored
            String userId = response.getUsername();
            return new StorageUser(Long.parseLong(userId), accessToken);
        } catch (KeyserverException ke) {
            throw new WebApplicationException(ACCESS_DENIED);
        }
    }
}
