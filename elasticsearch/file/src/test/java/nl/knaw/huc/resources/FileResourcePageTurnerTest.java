package nl.knaw.huc.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.huc.FileIndexer;
import nl.knaw.huc.FileConfiguration;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static javax.ws.rs.client.Entity.entity;
import static nl.knaw.huc.TestUtils.getResourceAsBytes;
import static nl.knaw.huc.service.JsonPathFactory.withJackson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.once;

@ExtendWith(DropwizardExtensionsSupport.class)
public class FileResourcePageTurnerTest {

  private static ClientAndServer mockServer;
  private static final int mockPort = 1080;

  private static final File configFile = new File("src/test/resources/test-config-pagesize-2.yml");

  public static DropwizardAppExtension<FileConfiguration> application;

  /*
    To test logging: create test app with DocumentResource and custom config and file logger
   */
  static {
    try {
      var factory = new YamlConfigurationFactory<>(
          FileConfiguration.class,
          Validators.newValidator(),
          Jackson.newObjectMapper(),
          "dw"
      );

      var fileConfiguration = factory.build(configFile);
      application = new DropwizardAppExtension<>(FileIndexer.class, fileConfiguration);

    } catch (IOException | ConfigurationException ex) {
      throw new RuntimeException("Could not init test app", ex);
    }
  }

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
  public void testFields_retrievesAllVersionPages() throws IOException {
    // start resource mocks:
    var mockRequests = startTextrepoMockServer();
    // start version page resource mocks:
    var versionsEndpoint = "/rest/files/[a-f0-9-]*/versions";
    mockRequests.add(mockEndpointPage(versionsEndpoint, "textrepo-versions-page1.json", 0, 2));
    mockRequests.add(mockEndpointPage(versionsEndpoint, "textrepo-versions-page2.json", 2, 2));
    mockRequests.add(mockEndpointPage(versionsEndpoint, "textrepo-versions-page3.json", 4, 2));
    // create test file:
    var fileContents = getResourceAsBytes("file.txt");
    var fileId = UUID.randomUUID().toString();

    var response = postTestContents(fileContents, "text/plain", fileId);
    var fields = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(200);
    var json = JsonPath.parse(fields);
    assertThat(json.read("$.file.id", String.class)).isEqualTo(fileId);
    // check versions order:
    assertThat(json.read("$.versions.length()", Integer.class)).isEqualTo(5);
    assertThat(json.read("$.versions[0].id", String.class)).contains("3333");
    assertThat(json.read("$.versions[1].id", String.class)).contains("2222");
    assertThat(json.read("$.versions[2].id", String.class)).contains("1111");
    assertThat(json.read("$.versions[3].id", String.class)).contains("0000");
    assertThat(json.read("$.versions[4].id", String.class)).contains("9999");
    // check contentsModified:
    assertThat(json.read("$.versions[0].contentsModified", Boolean.class)).isFalse();
    assertThat(json.read("$.versions[1].contentsModified", Boolean.class)).isTrue();
    assertThat(json.read("$.versions[2].contentsModified", Boolean.class)).isFalse();
    assertThat(json.read("$.versions[3].contentsModified", Boolean.class)).isFalse();
    assertThat(json.read("$.versions[4].contentsModified", Boolean.class)).isTrue();
    // check contentsLastModified:
    assertThat(json.read("$.contentsLastModified.contentsSha", String.class))
        .isEqualTo("22224942a9d96e2965f2a0f9d06b5878822111580fe061b038720330");
    assertThat(json.read("$.contentsLastModified.versionId", String.class))
        .isEqualTo("22220128-02be-4938-ba84-8d9dd70e19a5");
    assertThat(json.read("$.contentsLastModified.dateTime", String.class))
        .isEqualTo("2000-01-02T00:00:00");

    // check indexer has requested resources from all TR endpoints:
    mockRequests.forEach((mr) -> mockServer.verify(mr, once()));
  }

  @Test
  public void testFields_handlesZeroVersions() throws IOException {
    // start resource mocks:
    var mockRequests = startTextrepoMockServer();
    // start version page resource mocks:
    var versionsEndpoint = "/rest/files/[a-f0-9-]*/versions";
    mockRequests.add(mockEndpointPage(versionsEndpoint, "textrepo-versions-page1-no-versions.json", 0, 2));
    // create test file:
    var fileContents = getResourceAsBytes("file.txt");
    var fileId = UUID.randomUUID().toString();

    var response = postTestContents(fileContents, "text/plain", fileId);
    var fields = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(200);
    var json = JsonPath.parse(fields);
    assertThat(json.read("$.file.id", String.class)).isEqualTo(fileId);
    // check versions array is empty:
    assertThat(json.read("$.versions.length()", Integer.class)).isEqualTo(0);
    // check contentsLastModified is empty:
    var node = withJackson().parse(fields).read("$.contentsLastModified", JsonNode.class);
    assertThat(node.isEmpty()).isTrue();
    // check indexer has requested resources from all TR endpoints:
    mockRequests.forEach((mr) -> mockServer.verify(mr, once()));
  }

  private String getHost() {
    return "http://localhost:" + application.getLocalPort();
  }

  private ArrayList<HttpRequest> startTextrepoMockServer() throws IOException {
    var requests = new ArrayList<HttpRequest>();

    requests.add(mockEndpoint("/rest/files/[a-f0-9-]*", "textrepo-file.json"));
    requests.add(mockEndpoint("/rest/files/[a-f0-9-]*/metadata", "textrepo-file-metadata.json"));
    requests.add(mockEndpoint("/rest/documents/[a-f0-9-]*", "textrepo-doc.json"));
    requests.add(mockEndpoint("/rest/documents/[a-f0-9-]*/metadata", "textrepo-doc-metadata.json"));
    requests.add(mockEndpoint("/rest/types/[0-9]*", "textrepo-type.json"));

    return requests;
  }


  private HttpRequest mockEndpointPage(String versionsEndpoint, String versionPage, int offset, int limit)
      throws IOException {
    var request = request()
        .withMethod("GET")
        .withPath(versionsEndpoint)
        .withQueryStringParameter("limit", "" + limit)
        .withQueryStringParameter("offset", "" + offset);
    mockServer.when(
        request
    ).respond(
        response()
            .withStatusCode(200)
            .withHeader("content-type: application/json")
            .withBody(getResourceAsBytes(versionPage))
    );
    return request;
  }

  private HttpRequest mockEndpoint(String endpoint, String responseFilename) throws IOException {
    var request = request()
        .withMethod("GET")
        .withPath(endpoint);
    mockServer.when(
        request
    ).respond(
        response()
            .withStatusCode(200)
            .withHeader("content-type: application/json")
            .withBody(getResourceAsBytes(responseFilename))
    );
    return request;
  }

  private Response postTestContents(String bytes, String mimetype, String uuid) {

    var contentDisposition = FormDataContentDisposition
        .name("file")
        .size(bytes.toCharArray().length)
        .build();

    var bodyPart = new FormDataBodyPart(contentDisposition, bytes, MediaType.valueOf(mimetype));

    bodyPart.getHeaders().add("Link", "</rest/files/" + uuid + ">; rel=\"original\"");

    var multiPart = new FormDataMultiPart()
        .bodyPart(bodyPart);

    var request = application
        .client()
        .register(MultiPartFeature.class)
        .target(getHost() + "/file/fields")
        .request();

    var entity = entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }

}
