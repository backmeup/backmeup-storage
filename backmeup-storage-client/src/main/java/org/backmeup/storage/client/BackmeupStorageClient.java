package org.backmeup.storage.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.backmeup.storage.api.StorageClient;
import org.backmeup.storage.client.config.Configuration;
import org.backmeup.storage.client.model.auth.AuthInfo;
import org.backmeup.storage.model.Metadata;
import org.backmeup.storage.model.StorageUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BackmeupStorageClient implements StorageClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackmeupStorageClient.class);

    private static final String FILE_RESOURCE = "/files";
    private static final String FILE_RESOURCE_RIGHTS = "/files/rights";
    private static final String AUTH_RESOURCE = "/authenticate";

    private final String serviceUrl;

    private final CloseableHttpClient client;

    // Constructors -----------------------------------------------------------

    public BackmeupStorageClient() {
        this(Configuration.getProperty("backmeup.storage.service.url"));
    }

    public BackmeupStorageClient(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        this.client = createClient();
    }

    private CloseableHttpClient createClient() {
        // set the connection timeout value to 10 seconds (10000 milliseconds)
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10 * 1000).setSocketTimeout(10 * 1000).build();
        return HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
    }

    // Public Methods ---------------------------------------------------------

    @Override
    public String authenticate(String username, String password) throws IOException {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username must not be null or empty");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password must not be null or empty");
        }

        URIBuilder uriBuilder;
        URI url = null;
        try {
            uriBuilder = new URIBuilder(this.serviceUrl);

            uriBuilder.setPath((uriBuilder.getPath() + AUTH_RESOURCE).replaceAll("//+", "/"));
            uriBuilder.addParameter("username", username);
            uriBuilder.addParameter("password", password);

            url = uriBuilder.build();
        } catch (URISyntaxException e) {
            LOGGER.error("", e);
        }

        HttpGet request = new HttpGet(url);

        CloseableHttpResponse response = this.client.execute(request);

        int status = response.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK != status) {
            LOGGER.error("Request failed with status code: " + status);
            throw new IllegalStateException("Failed to authenticate user");
        }

        try {
            HttpEntity respEntity = response.getEntity();

            ObjectMapper mapper = createJsonMapper();
            AuthInfo authInfo = mapper.readValue(respEntity.getContent(), AuthInfo.class);

            // release all resources held by httpentity
            EntityUtils.consume(respEntity);

            return authInfo.getAccessToken();
        } catch (Exception e) {
            LOGGER.error("", e);
            //            throw new StorageClientException("xxx");
            throw new RuntimeException("Mapping of auth info failed", e);
        } finally {
            response.close();
        }
    }

    @Override
    public Metadata saveFile(String accessToken, String targetPath, boolean overwrite, long numBytes, InputStream data) throws IOException {
        LOGGER.info("URL: " + this.serviceUrl + "");

        URI full = null;
        try {
            URI base = new URI(this.serviceUrl + FILE_RESOURCE);
            full = new URI(base.getScheme(), base.getAuthority(), base.getPath().replaceAll("//", "/") + targetPath,
                    overwrite ? "overwrite=true" : null, null);
        } catch (URISyntaxException e) {
            LOGGER.error("cannot parse uri", e);
            throw new IOException(e);
        }

        HttpPut request = new HttpPut(full);
        request.setHeader("Accept", "application/json");
        request.setHeader("Authorization", accessToken);
        InputStreamEntity reqEntity = new InputStreamEntity(data, numBytes, ContentType.APPLICATION_OCTET_STREAM);
        //        reqEntity.setChunked(true);
        request.setEntity(reqEntity);

        CloseableHttpResponse response = this.client.execute(request);
        int status = response.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK != status) {
            LOGGER.error("Request failed with status code: " + status);
            return null;
        }

        try {
            HttpEntity respEntity = response.getEntity();
            //            String json = EntityUtils.toString(respEntity, "UTF-8");
            //            LOGGER.info(json);

            ObjectMapper mapper = createJsonMapper();
            Metadata metadata = mapper.readValue(respEntity.getContent(), Metadata.class);

            // release all resources held by httpentity
            EntityUtils.consume(respEntity);

            return metadata;
        } catch (Exception e) {
            LOGGER.error("", e);
            return null;
        } finally {
            response.close();
        }
    }

    @Override
    public void getFile(String accessToken, String path, OutputStream data) throws IOException {
        URI full = null;
        try {
            URI base = new URI(this.serviceUrl + FILE_RESOURCE);
            full = new URI(base.getScheme(), base.getAuthority(), base.getPath().replaceAll("//", "/") + path, null, null);
        } catch (URISyntaxException e) {
            LOGGER.error("cannot parse uri", e);
            throw new IOException(e);
        }

        HttpGet httpGet = new HttpGet(full);
        httpGet.addHeader("Authorization", accessToken);
        CloseableHttpResponse response = this.client.execute(httpGet);

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

    @Override
    public void addFileAccessRights(String accessToken, String ownerId, String filePath, String kscurrUserId, Long BMUcurrUserId) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeFileAccessRights(StorageUser user, String filePath) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean hasFileAccessRights(String accessToken, String ownerId, String filePath, String kscurrUserId, Long BMUcurrUserId,
            Long checkUserId) throws IOException {
        URI full = null;
        try {
            URI base = new URI(this.serviceUrl + FILE_RESOURCE_RIGHTS);
            full = new URI(base.getScheme(), base.getAuthority(), base.getPath().replaceAll("//", "/") + "/" + ownerId + "/" + filePath,
                    "accesstoken=" + accessToken + "&ksuserid=" + kscurrUserId + "&bmuuserid=" + BMUcurrUserId
                            + (checkUserId != null ? "&checkuserid=" + checkUserId : ""), null);
        } catch (URISyntaxException e) {
            LOGGER.error("cannot parse uri", e);
            throw new IOException(e);
        }

        HttpGet httpGet = new HttpGet(full);
        //httpGet.addHeader("Authorization", accessToken);
        CloseableHttpResponse response = this.client.execute(httpGet);

        int status = response.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK != status) {
            LOGGER.error("Request failed with status code: " + status);
            throw new IOException("Request failed with status code: " + status);
        }

        try {
            HttpEntity respEntity = response.getEntity();
            ObjectMapper mapper = createJsonMapper();
            Boolean hasAccess = mapper.readValue(respEntity.getContent(), Boolean.class);
            // release all resources held by httpentity
            EntityUtils.consume(respEntity);
            return hasAccess;
        } catch (Exception e) {
            LOGGER.error("", e);
            throw new IOException(e);
        } finally {
            response.close();
        }
    }

    private ObjectMapper createJsonMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        return objectMapper;
    }

}
