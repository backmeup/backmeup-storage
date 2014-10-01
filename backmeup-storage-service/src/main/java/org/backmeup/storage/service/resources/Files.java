package org.backmeup.storage.service.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

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

import org.backmeup.storage.model.dto.Metadata;
import org.backmeup.storage.model.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/files")
public class Files {
	private static final String DIGEST_ALGORITHM = "MD5";
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
		
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(DIGEST_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("", e);
		}
		long totalLength = 0;
		
		try (OutputStream out = new FileOutputStream(file);) {
			InputStream content = request.getInputStream();
			byte[] buffer = new byte[1024];
			int len;
			while ((len = content.read(buffer)) != -1) {
				out.write(buffer, 0, len);
				md.update(buffer, 0, len);
				totalLength += len;
			}
		} catch (IOException e) {
			LOGGER.error("", e);
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
		
		String hash;
		try {
			hash = StringUtils.getHexString(md.digest());
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("", e);
			hash = "-1";
		}
		Metadata fileMetadata = new Metadata(totalLength, hash, new Date(), filePath);

		return Response.ok(fileMetadata).build();
	}
}
