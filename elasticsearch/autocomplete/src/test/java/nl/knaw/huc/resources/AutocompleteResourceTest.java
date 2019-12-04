package nl.knaw.huc.resources;

import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.knaw.huc.AutocompleteApplication;
import nl.knaw.huc.AutocompleteConfiguration;
import nl.knaw.huc.service.FieldsService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.TestUtils.getResourceAsBytes;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

public class AutocompleteResourceTest {

  private static FieldsService FILE_SERVICE = mock(FieldsService.class);

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
  public void testFields_returns200_whenTxt() throws IOException {
    var fileContents = getResourceAsBytes("file.txt");
    var response = postTestContents(fileContents, "text/plain");
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  public void testFields_returns422UnprocessableEntity_whenPdf() throws IOException {
    var fileContents = getResourceAsBytes("file.pdf");
    var response = postTestContents(fileContents, "application/pdf");
    assertThat(response.getStatus()).isEqualTo(422);
    var entity = response.readEntity(String.class);
    System.out.println("entity:" + entity);
    assertThat(entity).contains("Unexpected mimetype: got [application/pdf] but should be one of [");
  }

  @Test
  public void testFields_returnsCompletionSuggesterInput() throws IOException {
    var fileContents = getResourceAsBytes("file.xml");
    var response = postTestContents(fileContents, "application/xml");
    assertThat(response.getStatus()).isEqualTo(200);
  }

  private Response postTestContents(byte[] bytes, String mimetype) {
    var contentDisposition = FormDataContentDisposition
        .name("contents")
        .size(bytes.length)
        .build();

    var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(contentDisposition, bytes, APPLICATION_OCTET_STREAM_TYPE));

    var port = RULE.getLocalPort();
    var request = client
        .target(format("http://localhost:%d/autocomplete/fields", port))
        .queryParam("mimetype", URLEncoder.encode(mimetype, UTF_8))
        .request();

    var entity = entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }

}
