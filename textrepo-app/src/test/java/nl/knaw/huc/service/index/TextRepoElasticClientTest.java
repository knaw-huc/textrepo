package nl.knaw.huc.service.index;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.huc.service.index.config.ElasticsearchConfiguration;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.client.RequestOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockitoAnnotations;
import org.mockserver.integration.ClientAndServer;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.once;

@ExtendWith(DropwizardExtensionsSupport.class)
public class TextRepoElasticClientTest {

  private static ClientAndServer mockServer;
  private static final int mockPort = 80;

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



  @ParameterizedTest
  @CsvSource({
      "localhost:9200,http://localhost:9200",
      "localhost,http://localhost",
      "127.0.0.1:8080:8080,http://127.0.0.1:8080:8080",
      "example:8080,http://example:8080",
      "example,http://example",
      "http://www.example.com:9200,http://www.example.com:9200",
      "https://www.example.com:9200,https://www.example.com:9200",
      "2001:0db8:85a3:0000:0000:8a2e:0370:7334:9200,http://2001:0db8:85a3:0000:0000:8a2e:0370:7334:9200",
  })
  public void newClient_shouldParseAddressesCorrectly(String in, String expected) {
    var config = new ElasticsearchConfiguration();
    config.hosts = List.of(in);
    var client = new TextRepoElasticClient(config);
    var toTest = client.getClient().getLowLevelClient().getNodes().get(0).getHost().toString();
    assertThat(toTest).isEqualTo(expected);
  }

  @Test
  public void newClient_usesPort80ByDefault() throws IOException {
    var request = request();
    mockServer.when(
        request)
              .respond(
        response()
            .withStatusCode(200)
            .withBody("{\"_index\": \"foo\", \"_type\": \"_doc\", \"_id\": \"bar\", \"found\": false}")
            .withHeader("Content-Type", "application/json")
    );

    var config = new ElasticsearchConfiguration();
    config.hosts = List.of("localhost");
    var client = new TextRepoElasticClient(config);
    var getRequest = new GetRequest("foo");
    getRequest.id("bar");

    client.getClient().get(getRequest, RequestOptions.DEFAULT);

    mockServer.verify(request, once());
  }

}
