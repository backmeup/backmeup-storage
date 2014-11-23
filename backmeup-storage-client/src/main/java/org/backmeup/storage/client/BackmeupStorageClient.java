package org.backmeup.storage.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.backmeup.storage.client.config.Configuration;
import org.backmeup.storage.model.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackmeupStorageClient implements StorageClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackmeupStorageClient.class);
    
    private static final String REGEX_MATCH_DOUBLE_SLASH = "(?<!(http:|https:))//";
    
    private static final String FILE_RESOURCE = "/files";
    
    private final String serviceUrl;

    private final CloseableHttpClient client;

    // Constructors -----------------------------------------------------------

    public BackmeupStorageClient() {
        this(Configuration.getProperty("backmeup.storage.service.url"));
    }
    
    public BackmeupStorageClient(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        this.client = HttpClients.createDefault();
    }

    // Public Methods ---------------------------------------------------------

    @Override
    public Metadata saveFile(String accessToken, String targetPath, boolean overwrite, long numBytes, InputStream data) throws IOException {
        LOGGER.info("URL: " + serviceUrl + "");
        
        StringBuilder sb = new StringBuilder();
        sb.append(serviceUrl);
        sb.append("FILE_RESOURCE");
        sb.append(targetPath);
        if(overwrite){
            sb.append("?overwrite=");
            sb.append(overwrite);
        }
        String url = sb.toString();
        url = url.replaceAll(REGEX_MATCH_DOUBLE_SLASH, "/");
        
        HttpPut request = new HttpPut(url);
        request.setHeader("Accept", "application/json");
        request.setHeader("Authorization", accessToken);
        InputStreamEntity reqEntity = new InputStreamEntity(data, numBytes, ContentType.APPLICATION_OCTET_STREAM);
//        reqEntity.setChunked(true);
        request.setEntity(reqEntity);

        CloseableHttpResponse response = client.execute(request);
        int status = response.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK != status) {
            LOGGER.error("Request failed with status code: " + status);
            return null;
        }
        
        try {
            HttpEntity respEntity = response.getEntity();
            String json = EntityUtils.toString(respEntity, "UTF-8");
            LOGGER.info(json);
            EntityUtils.consume(respEntity);
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            response.close();
        }

        return null;
    }

    @Override
    public void getFile(String accessToken, String path, OutputStream data) throws IOException {
        String url = serviceUrl + FILE_RESOURCE + path;
        url = url.replaceAll(REGEX_MATCH_DOUBLE_SLASH, "/");
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Authorization", accessToken);
        CloseableHttpResponse response = client.execute(httpGet);

        int status = response.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK != status) {
            LOGGER.error("Request failed with status code: " + status);
            return;
        }

        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                entity.writeTo(data);
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            response.close();
        }  
    }
}
