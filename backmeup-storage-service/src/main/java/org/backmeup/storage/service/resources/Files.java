package org.backmeup.storage.service.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/files")
public class Files {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@GET
    @Path("/{path}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getFile(@PathParam("path") String filePath) {
		return Response.ok().build();
	}
	
	@PUT
    @Path("/{path}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response putFile(@Context HttpServletRequest request, @PathParam("path") String filePath) {
		return Response.ok().build();
	}
}
