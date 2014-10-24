package org.backmeup.storage.service.resources;

import static com.jayway.restassured.RestAssured.given;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.backmeup.storage.logic.StorageLogic;
import org.backmeup.storage.logic.impl.DummyStorage;
import org.backmeup.storage.service.EmbeddedTestServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;

public class FilesTest {
    private static final String HOST = "http://localhost";
    private static final int PORT = 7654;

    @Rule
    public final EmbeddedTestServer SERVER = new EmbeddedTestServer(PORT, FilesWithMockedLogic.class);

    private static final File FILE = new File(FilesTest.class.getResource("/file.txt").getFile());

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
    public void testPutFileNoOverwrite() {
    }

    @Test
    public void testPutFileOverwrite() {
    }

    @Test
    public void testGetFileSimplePath() {
        //		ValidatableResponse response = 
        given()
        .log().all()
        .when()
        .get("/files/file.txt")
        .then()
        .log().all()
        .statusCode(200);
    }

    @Test
    public void testGetFileLongPath() {
        //		ValidatableResponse response = 
        given()
        .log().all()
        .when()
        .get("/files/path/to/file.txt")
        .then()
        .log().all()
        .statusCode(200);
    }

    private void assertStatusCode(int expectedStatus, HttpResponse response) {
        int responseCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(expectedStatus, responseCode);
    }

    // Mocks ------------------------------------------------------------------

    public static class FilesWithMockedLogic extends Files {
        @Override
        public StorageLogic getStorageLogic() {
            Map<String, File> map = new HashMap<>();
            map.put(FILE.getName(), FILE);
            map.put("path/to/file.txt", FILE);

            StorageLogic logic = new DummyStorage(map);
            return logic;
        }
    }
}
