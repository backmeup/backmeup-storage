package org.backmeup.storage.service.resources;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.backmeup.storage.service.EmbeddedTestServer;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class FilesTest {
	@Rule
	public final EmbeddedTestServer SERVER = new EmbeddedTestServer(Files.class);
	private final String HOST = SERVER.host;
	private final int PORT = SERVER.port;

	private HttpClient client = new DefaultHttpClient();

	@Test
	public void testPutFileNoOverwrite() {

	}

	@Test
	public void testPutFileOverwrite() {

	}

	@Test
	public void testGetFile() {

	}

	private void assertStatusCode(int expectedStatus, HttpResponse response) {
		int responseCode = response.getStatusLine().getStatusCode();
		Assert.assertEquals(expectedStatus, responseCode);
	}
}
