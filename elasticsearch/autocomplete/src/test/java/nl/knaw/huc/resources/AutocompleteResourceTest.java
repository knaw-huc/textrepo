package nl.knaw.huc.resources;

import com.jayway.jsonpath.JsonPath;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.knaw.huc.AutocompleteApplication;
import nl.knaw.huc.AutocompleteConfiguration;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URLEncoder;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.TestUtils.getResourceAsBytes;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AutocompleteResourceTest {

  @ClassRule
  public static DropwizardAppRule<AutocompleteConfiguration> RULE =
      new DropwizardAppRule<>(AutocompleteApplication.class, System.getProperty("user.dir") + "/config.yml");

  @Before
  public void setUp() {
    client = RULE.client();
    client.register(MultiPartFeature.class);
  }

  private Client client;

  @Test
  public void testMapping_returnsMapping() throws IOException {
    var response = client
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

  private Response postTestContents(byte[] bytes, String mimetype) {
    var contentDisposition = FormDataContentDisposition
        .name("file")
        .size(bytes.length)
        .build();

    var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(contentDisposition, bytes, APPLICATION_OCTET_STREAM_TYPE));

    var request = client
        .target(getTestUrl("/fields"))
        .queryParam("mimetype", URLEncoder.encode(mimetype, UTF_8))
        .request();

    var entity = entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }

  private String getTestUrl(String endpoint) {
    var port = RULE.getLocalPort();
    var host = "http://localhost";
    return format("%s:%d/autocomplete%s", host, port, endpoint);
  }

}
