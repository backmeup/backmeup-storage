package org.backmeup.storage.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.backmeup.storage.service.filters.TimingResourceFilter;
import org.backmeup.storage.service.resources.Files;

public class StorageApplicaiton extends Application {
	private final Set<Class<?>> set = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();

    public StorageApplicaiton() {
        singletons.add(new Files());

        set.add(TimingResourceFilter.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return set;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
