package nl.knaw.huc.resources.rest;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.helpers.ContentsHelper;
import nl.knaw.huc.service.contents.ContentsService;
import nl.knaw.huc.service.store.ContentsStorage;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.knaw.huc.resources.ResourceTestUtils.responsePart;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class ContentsResourceTest {
  private static final ContentsStorage FILE_STORAGE = mock(ContentsStorage.class);
  private static final int CONTENT_DECOMPRESSION_LIMIT = 10;
  private static final ContentsHelper CONTENTS_HELPER = new ContentsHelper(CONTENT_DECOMPRESSION_LIMIT);

  private static final String sha224 = "55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6";
  private static final String contents = "hello test";
  private static final Contents TEXT_REPO_CONTENTS = new Contents(
      sha224,
      contents.getBytes()
  );

  public static final ResourceExtension resource = ResourceExtension
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new ContentsResource(new ContentsService(FILE_STORAGE), CONTENTS_HELPER))
      .build();

  @BeforeEach
  public void setup() {
  }

  @AfterEach
  public void teardown() {
    reset(FILE_STORAGE);
  }

  @Test
  public void testGetFileBySha224_returnsFileContents_whenFileExists() throws IOException {
    when(FILE_STORAGE.get(eq(sha224))).thenReturn(TEXT_REPO_CONTENTS);

    var response = resource.client().target("/rest/contents/" + sha224).request().get();
    var inputStream = response.readEntity(InputStream.class);
    var actualContents = IOUtils.toString(inputStream, UTF_8);
    assertThat(actualContents).isEqualTo(contents);
  }

  @Test
  public void testGetFileBySha224_returns400BadRequest_whenIllegalSha224() {
    var response = resource.client().target("/rest/contents/55d4c44f5bc05762d8807f75f3").request().get();
    assertThat(response.getStatus()).isEqualTo(400);

    var actualErrorMessage = responsePart(response, "$.message");
    assertThat(actualErrorMessage).contains("not a sha");
    assertThat(actualErrorMessage).contains("55d4c44f5bc05762d8807f75f3");
  }

  @Test
  public void testGetFileBySha224_returns404NotFound_whenNoSuchSha224Exists() {
    when(FILE_STORAGE.get(any())).thenThrow(new NotFoundException("File not found"));

    var response = resource.client().target("/rest/contents/" + sha224).request().get();
    assertThat(response.getStatus()).isEqualTo(404);

    var actualErrorMessage = responsePart(response, "$.message");
    assertThat(actualErrorMessage).contains("not found");
  }

}
