package org.backmeup.storage.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.backmeup.storage.service.filters.SecurityInterceptor;
import org.backmeup.storage.service.filters.TimingResourceFilter;
import org.backmeup.storage.service.resources.Files;

@ApplicationPath("/")
public class BackmeupStorageApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(Files.class);
        classes.add(TimingResourceFilter.class);
        classes.add(SecurityInterceptor.class);
        return classes;
    }
}
