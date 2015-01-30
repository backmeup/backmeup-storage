package org.backmeup.storage.service.resources;

import static com.jayway.restassured.RestAssured.given;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.backmeup.storage.logic.StorageLogic;
import org.backmeup.storage.logic.impl.DummyStorage;
import org.backmeup.storage.model.StorageUser;
import org.backmeup.storage.service.EmbeddedTestServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;

public class DownloadTest {
    private static final String HOST = "http://localhost";
    private static final int PORT = 7654;

    @Rule
    public final EmbeddedTestServer SERVER = new EmbeddedTestServer(PORT, DownloadWithMockedLogic.class);

    private static final File HTML_FILE = new File(DownloadTest.class.getResource("/file.html").getFile());
    private static final File JPG_FILE = new File(DownloadTest.class.getResource("/file.jpg").getFile());
    private static final File BIN_FILE = new File(DownloadTest.class.getResource("/file.bin").getFile());

    @BeforeClass
    public static void setUpBeforeClass() {			
        RestAssured.baseURI = HOST;
        RestAssured.port = PORT;
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.requestContentType("application/json");
    }

    @AfterClass
    public static void tearDownAfterClass() {
        RestAssured.reset();
    }

    @Test
    public void testDownloadHtmlFile() {
        given()
        .log().all()
        .when()
        .get("/download/file.html&accessToken=1")
        .then()
        .log().all()
        .contentType(MediaType.TEXT_HTML)
        .statusCode(200);
    }
    
    @Test
    public void testDownloadJpgFile() {
        given()
        .log().all()
        .when()
        .get("/download/file.jpg&accessToken=1")
        .then()
        .contentType("image/jpeg")
        .statusCode(200);
    }
    
    @Test
    public void testDownloadBinFile() {
        given()
        .log().all()
        .when()
        .get("/download/file.bin&accessToken=1")
        .then()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .statusCode(200);
    }

    // Mocks ------------------------------------------------------------------

    public static class DownloadWithMockedLogic extends Download {
        @Override
        public StorageLogic getStorageLogic() {
            Map<String, File> map = new HashMap<>();
            map.put(HTML_FILE.getName(), HTML_FILE);
            map.put(JPG_FILE.getName(), JPG_FILE);
            map.put(BIN_FILE.getName(), BIN_FILE);

            StorageLogic logic = new DummyStorage(map);
            return logic;
        }
        
        @Override
        protected StorageUser getUserFromAccessToken(String accessToken) {
            StorageUser dummy = new StorageUser(1L);
            return dummy;
        }
    }
}
