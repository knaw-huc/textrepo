package nl.knaw.huc.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static javax.ws.rs.client.Entity.entity;
import static nl.knaw.huc.TestUtils.getResourceAsBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

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
  public void testFields_returnsFileId() throws IOException {
    startTextrepoMockServer();
    var expectations = new ObjectMapper().writeValueAsString(mockServer.retrieveActiveExpectations(request()));
    System.out.println("expectations: " + expectations);
    var fileContents = getResourceAsBytes("file.txt");
    var fileId = UUID.randomUUID().toString();
    var response = postTestContents(fileContents, "text/plain", fileId);
    var fields = response.readEntity(String.class);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(JsonPath.parse(fields).read("$.file.id", String.class)).isEqualTo(fileId);
  }

  private void startTextrepoMockServer() throws IOException {
    mockServer.when(
        request()
            .withMethod("GET")
            .withPath("/rest/files/.*")
    ).respond(
        response()
            .withStatusCode(200)
            .withHeader("content-type: application/json")
            .withBody(getResourceAsBytes("tr-file.json"))
    );

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
