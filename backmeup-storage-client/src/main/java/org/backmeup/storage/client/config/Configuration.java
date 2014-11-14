package org.backmeup.storage.client.config;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private static final Properties PROPERTIES = new Properties();

    private static final String PROPERTYFILE = "backmeup-storage-client.properties";

    static {
        InputStream propsStream = null;
        try {
            propsStream = Configuration.class.getClassLoader().getResourceAsStream(PROPERTYFILE);
            if (propsStream != null) {
                PROPERTIES.load(propsStream);
            } else {
                throw new IOException("unable to load properties file: " + PROPERTYFILE);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        } finally {
            close(propsStream);
        }
    }

    private Configuration() {
        // Configuration is a utility class, therefore private constructor
    }

    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    private static void close(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }
}
