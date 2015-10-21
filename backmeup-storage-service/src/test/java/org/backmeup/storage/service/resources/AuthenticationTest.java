package org.backmeup.storage.service.resources;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.Date;
import java.util.List;

import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.model.dto.BackupJobExecutionDTO;
import org.backmeup.model.dto.WorkerConfigDTO;
import org.backmeup.model.dto.WorkerInfoDTO;
import org.backmeup.model.dto.WorkerMetricDTO;
import org.backmeup.service.client.BackmeupService;
import org.backmeup.service.client.model.auth.AuthInfo;
import org.backmeup.storage.service.EmbeddedTestServer;
import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;

public class AuthenticationTest {
    private static final String HOST = "http://localhost";
    private static final int PORT = 7654;

    private static final String AUTH_ACCESS_TOKEN = "4711-ABCD-#+#+";
    private static final Date AUTH_EXPIRES_DATE = new Date();

    @Rule
    public final EmbeddedTestServer SERVER = new EmbeddedTestServer(PORT, AuthenticationWithMockedLogic.class);

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
    public void testAuthenticate() {
        final String username = "user1";
        final String password = "password123!";

        given()
            .log().all()
        .when()
            .get("/authenticate?username=" + username + "&password=" + password)
        .then()
            .log().all()
            .statusCode(200)
            .body("accessToken", equalTo(AUTH_ACCESS_TOKEN))
            .body("expiresAt", equalTo(AUTH_EXPIRES_DATE.getTime()));
    }

    // Mocks ------------------------------------------------------------------

    public static class AuthenticationWithMockedLogic extends Authentication {
        @Override
        public BackmeupService getBackmeupService() {
            return new BackmeupServiceMock();
        }
    }

    public static class BackmeupServiceMock implements BackmeupService {

        @Override
        public AuthInfo authenticate(String username, String password) {
            return new AuthInfo(AUTH_ACCESS_TOKEN, AUTH_EXPIRES_DATE);
        }

        @Override
        public AuthInfo authenticateWorker(String workerId, String workerSecret) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BackupJobDTO getBackupJob(Long jobId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BackupJobDTO updateBackupJob(BackupJobDTO backupJob) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BackupJobExecutionDTO getBackupJobExecution(Long jobExecId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BackupJobExecutionDTO getBackupJobExecution(Long jobExecId,
                boolean redeemToken) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BackupJobExecutionDTO updateBackupJobExecution(
                BackupJobExecutionDTO jobExecution) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WorkerConfigDTO initializeWorker(WorkerInfoDTO workerInfo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addWorkerMetrics(List<WorkerMetricDTO> workerMetrics) {
            throw new UnsupportedOperationException();
        }
    }
}
