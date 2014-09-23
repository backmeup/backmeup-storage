package org.backmeup.storage.client.config;

import java.io.IOException;
import java.util.Properties;

public final class Configuration {
	private static final Properties properties = new Properties();
	
	private static final String PROPERTYFILE = "backmeup-storage-client.properties";
	
	static {
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			if (loader.getResourceAsStream(PROPERTYFILE) != null) {
				properties.load(loader.getResourceAsStream(PROPERTYFILE));
			} else {
				throw new IOException("unable to load properties file: " + PROPERTYFILE);
			}
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public static String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		if (value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}
}