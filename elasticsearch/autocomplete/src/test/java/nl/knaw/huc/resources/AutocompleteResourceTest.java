package nl.knaw.huc.resources;

import ch.qos.logback.classic.Level;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huc.service.FieldsService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.io.IOException;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.TestUtils.getResourceAsBytes;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

public class AutocompleteResourceTest {

  private static FieldsService FILE_SERVICE = mock(FieldsService.class);

  @ClassRule
  public static final ResourceTestRule resource = ResourceTestRule
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new AutocompleteResource(FILE_SERVICE))
      .build();

  @Test
  public void testFields_returnsCompletionSuggesterInput() throws IOException {
    var fileContents = getResourceAsBytes("file.xml");
    var response = postTestContents(fileContents);
    assertThat(response.getStatus()).isEqualTo(204);
  }

  private Response postTestContents(byte[] bytes) {
    var contentDisposition = FormDataContentDisposition
        .name("contents")
        .size(bytes.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(contentDisposition, bytes, APPLICATION_OCTET_STREAM_TYPE));

    final var request = resource
        .client()
        .register(MultiPartFeature.class)
        .target("/autocomplete/fields")
        .request();

    final var entity = Entity.entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }


}
