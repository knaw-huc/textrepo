package nl.knaw.huc.resources.rest;

import com.jayway.jsonpath.JsonPath;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.exceptions.MethodNotAllowedExceptionMapper;
import nl.knaw.huc.helpers.ContentsHelper;
import nl.knaw.huc.resources.view.ViewBuilderFactory;
import nl.knaw.huc.service.version.content.VersionContentsService;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(DropwizardExtensionsSupport.class)
public class VersionContentsResourceTest {
  private static final int CONTENT_DECOMPRESSION_LIMIT = 10;
  private static final ContentsHelper CONTENTS_HELPER = new ContentsHelper(CONTENT_DECOMPRESSION_LIMIT);

  public static final ResourceExtension resource = ResourceExtension
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new VersionContentsResource(mock(VersionContentsService.class),
          CONTENTS_HELPER,
          mock(ViewBuilderFactory.class)))
      .addResource(new MethodNotAllowedExceptionMapper())
      .build();

  @Test
  public void testPost_returns405WithCorrectMsgAndAllowedMethods() {

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
