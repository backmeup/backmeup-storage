package org.backmeup.storage.service.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/files")
public class Files {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private static final String BASE_PATH = "/data/backmeup-storage/test/";
	
	@GET
    @Path("/{path}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getFile(@PathParam("path") String filePath) {
		File file = new File(BASE_PATH + filePath);
		if (file == null || !file.exists()) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		return Response
				.ok(file)
				.header("Content-Disposition",
						"attachment; filename=" + file.getName()).build();
	}
	
	@PUT
    @Path("/{path}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response putFile(@Context HttpServletRequest request, 
							@PathParam("path") String filePath, 
							@QueryParam("overwrite") @DefaultValue("false") boolean overwrite,
							@HeaderParam("Content-Length") long contentLength) {
		if (contentLength <= 0) {
			throw new WebApplicationException(Status.LENGTH_REQUIRED);
		}
		
		File file = new File(BASE_PATH + filePath);
		if (file != null  && file.exists() && !overwrite) {
			throw new WebApplicationException(Status.CONFLICT);
		}
		try (OutputStream out = new FileOutputStream(file);) {
			InputStream content = request.getInputStream();
			byte[] buffer = new byte[1024];
			int len;
			while ((len = content.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
		} catch (IOException e) {
			LOGGER.error("", e);
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		} 

		return Response.ok().build();
	}
}
