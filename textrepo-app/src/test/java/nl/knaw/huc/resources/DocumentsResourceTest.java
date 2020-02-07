package nl.knaw.huc.resources;

import ch.qos.logback.classic.Level;
import com.jayway.jsonpath.JsonPath;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.resources.rest.DocumentsResource;
import nl.knaw.huc.service.DocumentService;
import nl.knaw.huc.service.FileService;
import nl.knaw.huc.service.FileMetadataService;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DocumentsResourceTest {

  private static final String TEST_EXTERNAL_ID = "test-external-id";

  private static final Jdbi jdbi = mock(Jdbi.class);
  private static final DocumentService documentService = mock(DocumentService.class);
  private static final FileService fileService = mock(FileService.class);
  private static final FileMetadataService metadataService = mock(FileMetadataService.class);

  // https://stackoverflow.com/questions/31730571/how-to-turn-on-tracing-in-a-unit-test-using-a-resourcetestrule
  // Actually has to go /before/ ResourceTestRule these days as bootstrap()ing guarded against multiple calls.
  static {
    BootstrapLogging.bootstrap(Level.DEBUG);
  }

  @ClassRule
  public static final ResourceTestRule resource = ResourceTestRule
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new DocumentsResource(documentService))
      .build();

  @Before
  public void setupMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void resetMocks() {
    reset(jdbi, fileService, documentService);
  }

  @Test
  public void getDocument_shouldAlsoReturnExternalId() {
    var docId = UUID.randomUUID();
    when(documentService.get(docId)).thenReturn(Optional.of(new Document(docId, TEST_EXTERNAL_ID)));
    var response = resource
        .client()
        .register(MultiPartFeature.class)
        .target("/rest/documents/" + docId)
        .request().get();
    assertThat(response.getStatus()).isEqualTo(200);
    var body = response.readEntity(String.class);
    String externalId = JsonPath.parse(body).read("$.externalId");
    assertThat(externalId).isEqualTo(TEST_EXTERNAL_ID);
  }

}
