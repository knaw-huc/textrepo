package nl.knaw.huc.resources;

import ch.qos.logback.classic.Level;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huc.service.DocumentService;
import nl.knaw.huc.service.FileService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.Entity;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DocumentsResourceTest {
  private static final String TEST_CONTENT = "hello test";
  private static final String TEST_FILENAME = "just-a-filename.txt";
  private static final String TEST_TYPE = "test-type";
  private static final UUID FILE_ID = UUID.randomUUID();
  private static final UUID DOC_ID = UUID.randomUUID();

  private static final Jdbi jdbi = mock(Jdbi.class);
  private static final DocumentService documentService = mock(DocumentService.class);
  private static final FileService fileService = mock(FileService.class);

  // https://stackoverflow.com/questions/31730571/how-to-turn-on-tracing-in-a-unit-test-using-a-resourcetestrule
  // Actually has to go /before/ ResourceTestRule these days as bootstrap()ing guarded against multiple calls.
  static {
    BootstrapLogging.bootstrap(Level.DEBUG);
  }

  @ClassRule
  public static final ResourceTestRule resource = ResourceTestRule
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new DocumentsResource(documentService, fileService))
      .build();

  @Before
  public void setupMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void resetMocks() {
    reset(jdbi, fileService);
  }

  @Test
  public void postTestDocument() {
    final var bytes = TEST_CONTENT.getBytes();
    final var contentDisposition = FormDataContentDisposition
        .name("contents")
        .fileName(TEST_FILENAME)
        .size(bytes.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(contentDisposition, bytes, APPLICATION_OCTET_STREAM_TYPE));

    final var request = resource
        .client()
        .register(MultiPartFeature.class)
        .target("/documents")
        .queryParam("type", TEST_TYPE)
        .request();

    final var entity = Entity.entity(multiPart, multiPart.getMediaType());

    when(fileService.createFile(any(String.class))).thenReturn(FILE_ID);
    when(documentService.createDocument(any(UUID.class))).thenReturn(DOC_ID);

    var response = request.post(entity);

    var type = ArgumentCaptor.forClass(String.class);
    verify(fileService).createFile(type.capture());
    assertThat(type.getValue()).isEqualTo(TEST_TYPE);

    var fileId = ArgumentCaptor.forClass(UUID.class);
    verify(documentService).createDocument(fileId.capture());
    assertThat(fileId.getValue()).isEqualTo(FILE_ID);

    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeaderString("Location")).endsWith("documents/" + DOC_ID.toString());
  }
}
