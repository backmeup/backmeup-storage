package org.backmeup.storage.service.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.backmeup.service.client.BackmeupService;
import org.backmeup.service.client.impl.BackmeupServiceClient;
import org.backmeup.storage.service.config.Configuration;

@ApplicationScoped
public class BackmeupServiceClientProducer {
    private static final String DEFAULT_PATH = "http://localhost:8080/backmeup-service-rest";
    private String servicePath;
    
    public BackmeupServiceClientProducer() {
        this.servicePath = Configuration.getProperty("backmeup.service.path", DEFAULT_PATH);
    }
    
    @Produces
    public BackmeupService getBackmeupService() {
        return new BackmeupServiceClient(servicePath);
    }
}
