package nl.knaw.huc.resources;

import com.jayway.jsonpath.JsonPath;
import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.db.FileDAO;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


public class FilesResourceTest {

  private static final Jdbi jdbi = mock(Jdbi.class);
  private static final FileDAO fileDao = mock(FileDAO.class);

  private final static String sha224 = "55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6";
  private final static String content = "hello test";
  private final static TextRepoFile textRepoFile = new TextRepoFile(
    sha224,
    content.getBytes()
  );

  @ClassRule
  public static final ResourceTestRule resource = ResourceTestRule
    .builder()
    .addProvider(MultiPartFeature.class)
    .addResource(new FilesResource(jdbi))
    .build();

  @Before
  public void setup() {
    when(jdbi.onDemand(any())).thenReturn(fileDao);
    when(fileDao.findBySha224(eq(sha224))).thenReturn(textRepoFile);
  }

  @After
  public void teardown() {
    reset(jdbi);
    reset(fileDao);
  }

  @Test
  public void testPostFile() {
    var multiPart = new FormDataMultiPart()
      .field("file", content);

    var response = resource
      .client()
      .register(MultiPartFeature.class)
      .target("/files")
      .request()
      .post(Entity.entity(multiPart, multiPart.getMediaType()), String.class);

    String actualSha = JsonPath.parse(response).read("$.sha");
    assertThat(actualSha).isEqualTo(sha224);
  }

  @Test
  public void testGetFile() throws IOException {
    when(jdbi.onDemand(any())).thenReturn(fileDao);
    when(fileDao.findBySha224(eq(sha224))).thenReturn(textRepoFile);

    var response = resource
      .client()
      .target("/files/" + sha224)
      .request()
      .get();

    var inputStream = response.readEntity(InputStream.class);
    var actualContent = IOUtils.toString(inputStream, UTF_8);

    assertThat(actualContent).isEqualTo(content);
  }

}
