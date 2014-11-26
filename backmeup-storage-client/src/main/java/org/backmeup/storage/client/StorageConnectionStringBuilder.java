package org.backmeup.storage.client;

import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;


/*
 * [service-name];[[protocoll://]serverName[:portNumber][/basePath]][;property=value]*
 * 
 * backmeup-storage;[[protocoll://]serverName[:portNumber][/basePath]][;property=value]*
 * 
 * backmeup-storage;http://localhost:8080/backmeup-storage-service/;Token=abc123
 * 
 */
public class StorageConnectionStringBuilder {
    private static final String PREFIX = "backmeup-storage";
    private static final String PROTOCOL = "protocol";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String PATH = "path";
       
    private HashMap<String, String> url;
    private HashMap<String, String> properties;
    
    // Constructors -----------------------------------------------------------
    public StorageConnectionStringBuilder() {
        url = new HashMap<>();
        properties = new HashMap<>();
    }
    
    public StorageConnectionStringBuilder(String connectionString) {
        this();
        parse(connectionString);
    }
    
    // Properties -------------------------------------------------------------
    
    public String getProtocol() {
        return url.get(PROTOCOL);
    }

    public void setProtocol(String protocol) {
        if (!protocol.equals("http") || !protocol.equals("http")) {
            throw new IllegalArgumentException(
                    String.format("Protocol '%1' not supported. Use http or https!",protocol));
        }
        url.put(PROTOCOL, protocol);
    }

    public String getHost() {
        return url.get(HOST);
    }

    public void setHost(String serverName) {
        url.put(HOST, removeSlashes(serverName));
    }

    public int getPort() {
        return Integer.parseInt(url.get(PORT));
    }

    public void setPort(int portNumber) {
        url.put(PORT, Integer.toString(portNumber));
    }

    public void setPort(String portNumber) {
        setPort(Integer.parseInt(portNumber));
    }

    public String getPath() {
        return url.get(PATH);
    }

    public void setPath(String basePath) {
        url.put(PATH, basePath);
    }

    public void addProperty(String key, String value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Arguments must not be null");
        }
        properties.put(key, value);
    }
    
    public String getProperty(String key) {
        if (key == null || key == "") {
            throw new IllegalArgumentException("Key must not be null");
        }
        
        return properties.get(key);
    }
    
    public String getUrl() {
        StringBuffer sb = new StringBuffer();
        if (url.containsKey(PROTOCOL)) {
            sb.append(url.get(PROTOCOL));
            sb.append("://");
        }
        sb.append(url.get(HOST));
        if (url.containsKey(PORT)) {
            sb.append(":");
            sb.append(url.get(PORT));
        }
        if (url.containsKey(PATH)) {
            sb.append(url.get(PATH));
        }
        return sb.toString();
    }
    
    public void parse(String connectionString) {
        if (connectionString == null || connectionString.length() == 0) {
            throw new IllegalArgumentException("Invalid Connection String");
        }

        // 1: get name value pairs by splitting on the ';' character
        final String[] valuePairs = connectionString.split(";");

        // 2: parse prefix
        if (!valuePairs[0].equals(PREFIX)) {
            throw new IllegalArgumentException("Invalid Connection String");
        }

        // 3: parse server url
        final URL serverUrl;
        try {
            serverUrl = new URL(valuePairs[1]);

            setProtocol(serverUrl.getProtocol());
            setHost(serverUrl.getHost());
            if (serverUrl.getPort() != -1) {
                setPort(serverUrl.getPort());
            }
            setPath(serverUrl.getPath());

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Connection String"); 
        }

        // 4: for each remaining pair parse into appropriate map entries
        for (int i = 2; i < valuePairs.length; i++) {
            final int equalDex = valuePairs[i].indexOf("=");
            if (equalDex < 1) {
                throw new IllegalArgumentException("Invalid Connection String");
            }

            final String key = valuePairs[i].substring(0, equalDex);
            if (key == null || key.equals("")) {
                throw new IllegalArgumentException("Invalid Connection String");
            }

            final String value = valuePairs[i].substring(equalDex + 1);
            if (value == null || value.equals("")) {
                throw new IllegalArgumentException("Invalid Connection String");
            }

            addProperty(key, value);
        }
    }

    public String build() {
        return this.toString();
    }
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX);
        sb.append(";");
        sb.append(getUrl());
        if (!properties.isEmpty()) {
            for (Entry<String, String> entry : properties.entrySet()) {
                sb.append(";");
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(entry.getValue());
            }
        }
        return sb.toString();
    }
    
    // Private methods --------------------------------------------------------
    
    private String removeSlashes(String string) {
        // return string.replaceAll("/$", "");

        String retVal = string;
        // remove leading slash
        if (retVal.startsWith("/")) {
            retVal = retVal.substring(1, retVal.length());
        }

        // remove trailing slash
        if (retVal.endsWith("/")) {
            retVal = retVal.substring(0, retVal.length() - 1);
        }

        return retVal;
    }
}
