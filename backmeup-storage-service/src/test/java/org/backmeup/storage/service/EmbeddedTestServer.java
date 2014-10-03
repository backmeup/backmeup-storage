package org.backmeup.storage.service;

import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.junit.rules.ExternalResource;

public class EmbeddedTestServer extends ExternalResource {

	private static final String HOST = "http://localhost:";
	private static final int PORT = 7654;

	public final String host = HOST;
	public final int port = PORT;
	private final Class<?> resource;
	private TJWSEmbeddedJaxrsServer server;

	public EmbeddedTestServer(Class<?> resource) {
		this.resource = resource;
	}

	@Override
	protected void before() {
		server = new TJWSEmbeddedJaxrsServer();
		server.setPort(PORT);
		server.getDeployment().getActualResourceClasses().add(resource);
		server.start();
	}

	@Override
	protected void after() {
		server.stop();
	}
}
