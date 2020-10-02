package nl.knaw.huc.resources;

import com.jayway.jsonpath.JsonPath;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.FileConfiguration;
import nl.knaw.huc.service.FieldsService;
import nl.knaw.huc.service.MappingService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

import static javax.ws.rs.client.Entity.entity;
import static nl.knaw.huc.TestUtils.getResourceAsBytes;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class FileResourceTest {

  public static final ResourceExtension resource;

  static {
    var config = new FileConfiguration();
    config.setMappingFile("file-mapping.json");
    resource = ResourceExtension
        .builder()
        .addProvider(MultiPartFeature.class)
        .addResource(new FileResource(new FieldsService(), new MappingService(config)))
        .build();
  }

  @BeforeEach
  public void setup() {
  }

  @AfterEach
  public void teardown() {
  }

  @Test
  public void testMapping_returnsMapping() {
    var response = resource
        .target("/file/mapping")
        .request()
        .get();

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

    var request = resource
        .target("/file/fields")
        .register(MultiPartFeature.class)
        .request();

    var entity = entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }

}
