package nl.knaw.huc.service.health;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.huc.resources.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockserver.integration.ClientAndServer;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(DropwizardExtensionsSupport.class)
public class IndexerClientHealthCheckTest {

  private static ClientAndServer mockServer;
  private static final int mockPort = 1080;

  @BeforeAll
  public static void setUpClass() {
    mockServer = ClientAndServer.startClientAndServer(mockPort);
  }

  @BeforeEach
  public void before() {
    mockServer.reset();
    MockitoAnnotations.initMocks(this);
  }

  @AfterAll
  public static void tearDown() {
    mockServer.stop();
  }

  @Test
  public void check_returnsUnhealthyResult_whenHostDoesNotExist() {
    var toTest = new IndexerHealthCheck("http://does-not-exist:1234");

    var result = toTest.check();

    assertThat(result.isHealthy()).isEqualTo(false);
    assertThat(result.getMessage()).contains("Http status of mapping endpoint: unknown;");
  }

  @Test
  public void check_returnsUnhealthyResult_whenHostReturns500() throws IOException {
    var mapping = "/mapping";
    var host = "http://localhost:" + mockPort + mapping;
    mockServer.when(request()
        .withMethod("GET")
        .withPath(mapping)
    ).respond(response()
        .withStatusCode(500)
        .withHeader("Content-Type", "application/json")
        .withBody("Server error!")
    );

    var toTest = new IndexerHealthCheck(host);

    var result = toTest.check();

    assertThat(result.isHealthy()).isEqualTo(false);
    assertThat(result.getMessage()).isEqualTo("Http status of mapping endpoint: 500; reason: Server error!");
  }

  @Test
  public void check_returnsHealthyResult_whenHostReturns200() throws IOException {
    var mapping = "/mapping";
    var host = "http://localhost:" + mockPort + mapping;
    mockServer.when(request()
        .withMethod("GET")
        .withPath(mapping)
    ).respond(response()
        .withStatusCode(200)
        .withHeader("Content-Type", "application/json")
        .withBody("Up")
    );

    var toTest = new IndexerHealthCheck(host);

    var result = toTest.check();

    assertThat(result.isHealthy()).isEqualTo(true);
    assertThat(result.getMessage()).isEqualTo("Http status of mapping endpoint: 200");
  }


}
