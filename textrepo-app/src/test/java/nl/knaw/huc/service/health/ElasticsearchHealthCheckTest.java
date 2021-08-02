package nl.knaw.huc.service.health;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.huc.resources.TestUtils;
import nl.knaw.huc.service.index.config.ElasticsearchConfiguration;
import nl.knaw.huc.service.index.TextRepoElasticClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockserver.integration.ClientAndServer;

import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(DropwizardExtensionsSupport.class)
public class ElasticsearchHealthCheckTest {

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
    var toTest = createElasticsearchHealthCheck("http://test-host:42", "test-index-name");

    var result = toTest.check();

    assertThat(result.isHealthy()).isEqualTo(false);
    assertThat(result.getMessage()).contains("Health status: unknown;");
  }

  @Test
  public void check_returnsUnhealthyResult_whenEsHealthStatusRed() throws IOException {
    var indexName = "test-index-name";
    var host = "localhost:" + mockPort;
    var endpoint = "/_cluster/health/" + indexName;
    mockServer.when(request()
        .withMethod("GET")
        .withPath(endpoint)
    ).respond(response()
        .withStatusCode(200)
        .withHeader("Content-Type", "application/json")
        .withBody(TestUtils.getResourceAsString("health/unhealthyEsIndexResponse.json"))
    );
    var toTest = createElasticsearchHealthCheck(host, indexName);

    var result = toTest.check();

    assertThat(result.isHealthy()).isEqualTo(false);
    assertThat(result.getMessage()).contains("Health status: RED");
  }

  @Test
  public void check_returnsHealthyResult_whenEsHealthStatusYellow() throws IOException {
    var indexName = "test-index-name";
    var host = "localhost:" + mockPort;
    var endpoint = "/_cluster/health/" + indexName;
    mockServer.when(request()
        .withMethod("GET")
        .withPath(endpoint)
    ).respond(response()
        .withStatusCode(200)
        .withHeader("Content-Type", "application/json")
        .withBody(TestUtils.getResourceAsString("health/healthyEsIndexResponse.json"))
    );
    var toTest = createElasticsearchHealthCheck(host, indexName);

    var result = toTest.check();

    assertThat(result.isHealthy()).isEqualTo(true);
    assertThat(result.getMessage()).contains("Health status: YELLOW");
  }

  private ElasticsearchHealthCheck createElasticsearchHealthCheck(String host, String indexName) {
    var config = new ElasticsearchConfiguration();
    config.index = indexName;
    config.hosts = new ArrayList<>();
    config.hosts.add(host);
    var trEsClient = new TextRepoElasticClient(config);
    return new ElasticsearchHealthCheck(trEsClient);
  }

}
