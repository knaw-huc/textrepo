package nl.knaw.huc.resources.rest;

import com.jayway.jsonpath.JsonPath;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.exceptions.MethodNotAllowedExceptionMapper;
import nl.knaw.huc.service.VersionContentsService;
import nl.knaw.huc.service.store.ContentsStorage;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.UUID;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(DropwizardExtensionsSupport.class)
public class VersionContentsResourceTest {
  private static final ContentsStorage FILE_STORAGE = mock(ContentsStorage.class);

  public static final ResourceExtension resource = ResourceExtension
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new VersionContentsResource(mock(VersionContentsService.class)))
      .addResource(new MethodNotAllowedExceptionMapper())
      .build();

  @Test
  public void testPost_returns405WithCorrectMsgAndAllowedMethods() throws IOException {

    var response = resource
        .client()
        .target("/rest/versions/" + UUID.randomUUID() + "/contents/")
        .request()
        .post(entity("", APPLICATION_JSON_TYPE));

    var body = JsonPath.parse(response.readEntity(String.class));
    assertThat(body.read("$.code", Integer.class))
        .isEqualTo(405);
    assertThat(body.read("$.message", String.class))
        .contains("Not allowed to post contents");
  }

}
