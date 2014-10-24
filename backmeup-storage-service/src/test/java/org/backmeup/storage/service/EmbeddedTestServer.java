package org.backmeup.storage.service;

import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.junit.rules.ExternalResource;

public class EmbeddedTestServer extends ExternalResource {
    private final String HOST;
    private final int PORT;

    private final Class<?> resource;
    private TJWSEmbeddedJaxrsServer server;

    public EmbeddedTestServer(int port, Class<?> resource) {
        this.HOST = "http://localhost";
        this.PORT = port;
        this.resource = resource;
    }

    public String getHost() {
        return HOST;
    }

    public int getPort() {
        return PORT;
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
