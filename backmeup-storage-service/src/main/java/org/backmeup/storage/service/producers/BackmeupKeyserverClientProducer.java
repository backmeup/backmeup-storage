package org.backmeup.storage.service.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.backmeup.keyserver.client.KeyserverClient;
import org.backmeup.storage.service.config.Configuration;
import org.backmeup.utilities.Assert;

@ApplicationScoped
public class BackmeupKeyserverClientProducer {

    private String baseUrl = Configuration.getProperty("backmeup.keyserver.baseUrl");
    private String appId = Configuration.getProperty("backmeup.storage.appId");
    private String appSecret = Configuration.getProperty("backmeup.storage.appSecret");

    private KeyserverClient keyserverClient;

    @Produces
    @ApplicationScoped
    public KeyserverClient getKeyserverClient() {
        if (keyserverClient == null) {
            Assert.notNull(baseUrl, "Keyserver base url must not be null");
            Assert.notNull(appId, "Keyserver app id must not be null");
            Assert.notNull(appSecret, "Keyserver app secret must not be null");

            keyserverClient = new KeyserverClient(baseUrl, appId, appSecret);
        }
        return keyserverClient;
    }

}
