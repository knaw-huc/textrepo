package nl.knaw.huc.resources;

import com.jayway.jsonpath.JsonPath;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.huc.FileApplication;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.once;

@ExtendWith(DropwizardExtensionsSupport.class)
public class FileResourceTest {

  private static ClientAndServer mockServer;
  private static final int mockPort = 1080;

  private static final File configFile = new File("src/test/resources/test-config.yml");

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
      application = new DropwizardAppExtension<>(FileApplication.class, fileConfiguration);

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
  public void testMapping_returnsMapping() {
    var response = application
        .client()
        .target(getHost() + "/file/mapping")
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(200);
    var fields = response.readEntity(String.class);
    var dynamic = JsonPath.parse(fields).read("$.mappings.dynamic");
    assertThat(dynamic).isEqualTo(false);
    var fileIdtype = JsonPath.parse(fields).read("$.mappings.properties.file.properties.id.type");
    assertThat(fileIdtype).isEqualTo("keyword");
  }

  private String getHost() {
    return "http://localhost:" + application.getLocalPort();
  }

  @Test
  public void testFields_returnsFileDoc() throws IOException {
    var mockRequests = startTextrepoMockServer();
    var fileContents = getResourceAsBytes("file.txt");
    var fileId = UUID.randomUUID().toString();

    var response = postTestContents(fileContents, "text/plain", fileId);
    var fields = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(200);
    // file:
    assertThat(JsonPath.parse(fields).read("$.file.id", String.class)).isEqualTo(fileId);
    // file type:
    assertThat(JsonPath.parse(fields).read("$.file.type.id", Integer.class)).isEqualTo(3);
    assertThat(JsonPath.parse(fields).read("$.file.type.name", String.class)).isEqualTo("test-type");
    assertThat(JsonPath.parse(fields).read("$.file.type.mimetype", String.class)).isEqualTo("application/test");
    // file metadata:
    assertThat(JsonPath.parse(fields).read("$.file.metadata.foo", String.class)).isEqualTo("bar");
    assertThat(JsonPath.parse(fields).read("$.file.metadata.spam", String.class)).isEqualTo("eggs");
    // document:
    assertThat(JsonPath.parse(fields).read("$.doc.id", String.class)).isEqualTo("99999999-f0a0-406e-b01b-b12b9df9a84c");
    assertThat(JsonPath.parse(fields).read("$.doc.externalId", String.class))
        .isEqualTo("dat-pak-melk-buiten-de-koelkast");
    // document metadata:
    assertThat(JsonPath.parse(fields).read("$.doc.metadata.docfoo", String.class)).isEqualTo("docbar");
    assertThat(JsonPath.parse(fields).read("$.doc.metadata.docspam", String.class)).isEqualTo("doceggs");
    // versions:
    assertThat(JsonPath.parse(fields).read("$.versions[0].id", String.class))
        .isEqualTo("33330128-02be-4938-ba84-8d9dd70e19a5");
    assertThat(JsonPath.parse(fields).read("$.versions[0].contentsChanged", Boolean.class)).isEqualTo(true);
    assertThat(JsonPath.parse(fields).read("$.versions[0].createdAt", String.class)).isEqualTo("2000-01-03T00:00:00");
    assertThat(JsonPath.parse(fields).read("$.versions[1].id", String.class))
        .isEqualTo("22220128-02be-4938-ba84-8d9dd70e19a5");
    assertThat(JsonPath.parse(fields).read("$.versions[1].contentsChanged", Boolean.class)).isEqualTo(false);
    assertThat(JsonPath.parse(fields).read("$.versions[1].createdAt", String.class)).isEqualTo("2000-01-02T00:00:00");
    assertThat(JsonPath.parse(fields).read("$.versions[2].id", String.class))
        .isEqualTo("11110128-02be-4938-ba84-8d9dd70e19a5");
    assertThat(JsonPath.parse(fields).read("$.versions[2].contentsChanged", Boolean.class)).isEqualTo(true);
    assertThat(JsonPath.parse(fields).read("$.versions[2].createdAt", String.class)).isEqualTo("2000-01-01T00:00:00");
    // contentsLastModified:
    assertThat(JsonPath.parse(fields).read("$.contentsLastModified.dateTime", String.class))
        .isEqualTo("2000-01-03T00:00:00");
    assertThat(JsonPath.parse(fields).read("$.contentsLastModified.contentsSha", String.class))
        .isEqualTo("33334942a9d96e2965f2a0f9d06b5878822111580fe061b038720330");
    assertThat(JsonPath.parse(fields).read("$.contentsLastModified.versionId", String.class))
        .isEqualTo("33330128-02be-4938-ba84-8d9dd70e19a5");

    // check indexer requests all textrepo resource endpoints:
    mockRequests.forEach((mr) -> mockServer.verify(mr, once()));
  }

  private ArrayList<HttpRequest> startTextrepoMockServer() throws IOException {
    var requests = new ArrayList<HttpRequest>();
    requests.add(mockEndpoint("/rest/files/[a-f0-9-]*", "textrepo-file.json"));
    requests.add(mockEndpoint("/rest/files/[a-f0-9-]*/metadata", "textrepo-file-metadata.json"));
    requests.add(mockEndpoint("/rest/documents/[a-f0-9-]*", "textrepo-doc.json"));
    requests.add(mockEndpoint("/rest/documents/[a-f0-9-]*/metadata", "textrepo-doc-metadata.json"));
    requests.add(mockEndpoint("/rest/types/[0-9]*", "textrepo-type.json"));
    requests.add(mockEndpoint("/rest/files/[a-f0-9-]*/versions", "textrepo-versions.json"));
    return requests;
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
