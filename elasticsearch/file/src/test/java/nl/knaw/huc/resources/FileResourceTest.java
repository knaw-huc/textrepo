package nl.knaw.huc.resources;

import com.jayway.jsonpath.JsonPath;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.knaw.huc.FileApplication;
import nl.knaw.huc.FileConfiguration;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;
import static nl.knaw.huc.TestUtils.getResourceAsBytes;
import static org.assertj.core.api.Assertions.assertThat;

public class FileResourceTest {

  @ClassRule
  public static DropwizardAppRule<FileConfiguration> RULE = new DropwizardAppRule<>(
      FileApplication.class, System.getProperty("user.dir") + "/src/test/resources/test-config.yml"
  );

  @Before
  public void setUp() {
    client = RULE.client();
    client.register(MultiPartFeature.class);
  }

  private Client client;

  @Test
  public void testMapping_returnsMapping() {
    var response = client
        .target(getTestUrl("/mapping"))
        .request().get();

    assertThat(response.getStatus()).isEqualTo(200);
    var fields = response.readEntity(String.class);
    var dynamic = JsonPath.parse(fields).read("$.mappings.dynamic");
    assertThat(dynamic).isEqualTo(false);
    var fileIdtype = JsonPath.parse(fields).read("$.mappings.properties.id.type");
    assertThat(fileIdtype).isEqualTo("keyword");
  }

  @Test
  public void testFields_returnsFileId() throws IOException {
    var fileContents = getResourceAsBytes("file.txt");
    var fileId = UUID.randomUUID().toString();
    var response = postTestContents(fileContents, "text/plain", fileId);
    var fields = response.readEntity(String.class);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(JsonPath.parse(fields).read("$.id", String.class)).isEqualTo(fileId);
  }

  private Response postTestContents(byte[] bytes, String mimetype, String uuid) {
    var contentDisposition = FormDataContentDisposition
        .name("file")
        .size(bytes.length)
        .build();

    var bodyPart = new FormDataBodyPart(contentDisposition, bytes, MediaType.valueOf(mimetype));

    bodyPart.getHeaders().add("Link", "</rest/files/" + uuid + ">; rel=\"original\"");

    var multiPart = new FormDataMultiPart()
        .bodyPart(bodyPart);

    var request = client
        .target(getTestUrl("/fields"))
        .request();

    var entity = entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }

  private String getTestUrl(String endpoint) {
    var port = RULE.getLocalPort();
    var host = "http://localhost";
    return format("%s:%d/file%s", host, port, endpoint);
  }

}
