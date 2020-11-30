package nl.knaw.huc.resources;

import com.jayway.jsonpath.JsonPath;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.huc.AutocompleteConfiguration;
import nl.knaw.huc.AutocompleteIndexer;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;
import static nl.knaw.huc.TestUtils.getResourceAsBytes;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class AutocompleteResourceTest {

  private static ClientAndServer mockServer;
  private static final int mockPort = 1080;

  private static final File configFile = new File("src/test/resources/test-config.yml");

  public static DropwizardAppExtension<AutocompleteConfiguration> application;

  /*
    To test logging: create test app with DocumentResource and custom config and file logger
   */
  static {
    try {
      var factory = new YamlConfigurationFactory<>(
          AutocompleteConfiguration.class,
          Validators.newValidator(),
          Jackson.newObjectMapper(),
          "dw"
      );

      var fileConfiguration = factory.build(configFile);
      application = new DropwizardAppExtension<>(AutocompleteIndexer.class, fileConfiguration);

    } catch (IOException | ConfigurationException ex) {
      throw new RuntimeException("Could not init test app", ex);
    }
  }

  @Test
  public void testMapping_returnsMapping() throws IOException {
    var response = application
        .client()
        .register(MultiPartFeature.class)
        .target(getTestUrl("/mapping"))
        .request().get();

    assertThat(response.getStatus()).isEqualTo(200);
    var fields = response.readEntity(String.class);
    var suggestionType = JsonPath.parse(fields).read("$.mappings.properties.suggest.type");
    assertThat(suggestionType).isEqualTo("completion");
  }

  @Test
  public void testFields_returnsCompletionSuggesterInputSortedByWeight_whenTxt() throws IOException {
    var fileContents = getResourceAsBytes("file.txt");
    var response = postTestContents(fileContents, "text/plain");
    assertThat(response.getStatus()).isEqualTo(200);
    var fields = response.readEntity(String.class);

    // 'Heenbergen' is the most prevalent keyword, appearing 4 times:
    var firstInput = JsonPath.parse(fields).read("$.suggest[0].input");
    assertThat(firstInput).isEqualTo("Heenbergen");
    var firstWeight = JsonPath.parse(fields).read("$.suggest[0].weight");
    assertThat(firstWeight).isEqualTo(4);
  }

  @Test
  public void testFields_returnsCompletionSuggesterInput_whenXml() throws IOException {
    var fileContents = getResourceAsBytes("file.xml");
    var response = postTestContents(fileContents, "application/xml");
    assertThat(response.getStatus()).isEqualTo(200);
    var fields = response.readEntity(String.class);

    // 'Schiffer' is the most prevalent keyword, appearing 17 times:
    var firstInput = JsonPath.parse(fields).read("$.suggest[0].input");
    assertThat(firstInput).isEqualTo("Schiffer");
    var firstWeight = JsonPath.parse(fields).read("$.suggest[0].weight");
    assertThat(firstWeight).isEqualTo(17);
  }

  @Test
  public void testFields_returns422UnprocessableEntity_whenPdf() throws IOException {
    var fileContents = getResourceAsBytes("file.pdf");
    var response = postTestContents(fileContents, "application/pdf");
    assertThat(response.getStatus()).isEqualTo(422);
    var fields = response.readEntity(String.class);
    assertThat(fields).contains("Unexpected mimetype: got [application/pdf] but should be one of [");
  }

  @Test
  public void testFields_returnsCompletionSuggesterInput_whenPageXml() throws IOException {
    var fileContents = getResourceAsBytes("file.page.xml");
    var response = postTestContents(fileContents, "application/vnd.prima.page+xml");
    assertThat(response.getStatus()).isEqualTo(200);
    var fields = response.readEntity(String.class);
    var firstInput = JsonPath.parse(fields).read("$.suggest[0].input");
    assertThat(firstInput).isEqualTo("evolutie");
    var firstWeight = JsonPath.parse(fields).read("$.suggest[0].weight");
    assertThat(firstWeight).isEqualTo(1);
  }

  @Test
  public void testTypes_returnsMimetypesAndSubtypes() throws IOException {
    var response = application.client().target(getTestUrl("/types")).request().get();
    var types = response.readEntity(String.class);
    assertThat(types).isEqualTo("{\"types\":[{\"mimetype\":\"application/xml\",\"subtypes\":[\"" +
        "application/vnd.prima.page+xml\"]},{\"mimetype\":\"text/plain\",\"subtypes\":[]}]}");
  }

  private Response postTestContents(byte[] bytes, String mimetype) {
    var contentDisposition = FormDataContentDisposition
        .name("file")
        .size(bytes.length)
        .build();

    var bodyPart = new FormDataBodyPart(contentDisposition, bytes, MediaType.valueOf(mimetype));
    var multiPart = new FormDataMultiPart()
        .bodyPart(bodyPart);

    var request = application
        .client()
        .register(MultiPartFeature.class)
        .target(getTestUrl("/fields"))
        .request();

    var entity = entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }

  private String getTestUrl(String endpoint) {
    var port = application.getLocalPort();
    var host = "http://localhost";
    return format("%s:%d/autocomplete%s", host, port, endpoint);
  }

}
