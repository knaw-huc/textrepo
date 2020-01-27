package nl.knaw.huc.resources;

import ch.qos.logback.classic.Level;
import com.jayway.jsonpath.JsonPath;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextrepoFile;
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
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DocumentsResourceTest {
  private static final String TEST_CONTENT = "hello test";
  private static final String TEST_FILENAME = "just-a-filename.txt";
  private static final String TEST_TYPE = "test-type";
  private static final String TEST_EXTERNAL_ID = "test-external-id";
  private static final UUID TEST_FILE_ID = UUID.randomUUID();
  private static final UUID TEST_DOC_ID = UUID.randomUUID();

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
    reset(jdbi, fileService, documentService);
  }

  @Test
  public void postTestDocument() {
    when(fileService.createFile(any(String.class))).thenReturn(new TextrepoFile(TEST_FILE_ID, (short) 42));
    when(documentService.createDocumentByExternalId(any(UUID.class), anyString())).thenReturn(TEST_DOC_ID);

    var byExternalId = "false";
    var response = postTestFile(byExternalId);

    var type = ArgumentCaptor.forClass(String.class);
    verify(fileService).createFile(type.capture());
    assertThat(type.getValue()).isEqualTo(TEST_TYPE);

    var fileId = ArgumentCaptor.forClass(UUID.class);
    var externalId = ArgumentCaptor.forClass(String.class);
    verify(documentService).createDocumentByExternalId(fileId.capture(), externalId.capture());
    assertThat(fileId.getValue()).isEqualTo(TEST_FILE_ID);
    assertThat(externalId.getValue()).isEqualTo(TEST_EXTERNAL_ID);

    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeaderString("Location")).endsWith("documents/" + TEST_DOC_ID.toString() + "/" + TEST_TYPE);
  }

  @Test
  public void addDocument_shouldUseExternalId_whenByExternalIdIsTrue() {
    when(fileService.createFile(any(String.class)))
        .thenReturn(new TextrepoFile(TEST_FILE_ID, (short) 42));
    when(documentService.findDocumentByExternalId(TEST_EXTERNAL_ID))
        .thenReturn(Optional.of(TEST_DOC_ID));

    var byExternalId = "true";
    var response = postTestFile(byExternalId);

    var type = ArgumentCaptor.forClass(String.class);
    verify(fileService).createFile(type.capture());
    assertThat(type.getValue()).isEqualTo(TEST_TYPE);

    var docId = ArgumentCaptor.forClass(UUID.class);
    var fileId = ArgumentCaptor.forClass(UUID.class);
    verify(documentService).addFileToDocument(docId.capture(), fileId.capture());
    assertThat(docId.getValue()).isEqualTo(TEST_DOC_ID);
    assertThat(fileId.getValue()).isEqualTo(TEST_FILE_ID);

    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeaderString("Location")).endsWith("documents/" + TEST_DOC_ID.toString() + "/" + TEST_TYPE);
  }

  @Test
  public void getDocument_shouldAlsoReturnExternalId() {
    var docId = UUID.randomUUID();
    when(documentService.get(docId)).thenReturn(new Document(docId, TEST_EXTERNAL_ID));
    var response = resource
        .client()
        .register(MultiPartFeature.class)
        .target("/documents/" + docId)
        .request().get();
    assertThat(response.getStatus()).isEqualTo(200);
    var body = response.readEntity(String.class);
    String externalId = JsonPath.parse(body).read("$.externalId");
    assertThat(externalId).isEqualTo(TEST_EXTERNAL_ID);
  }

  @Test
  public void updateDocumentByExternalIdAndType_shouldUpdateDocument() {
    when(documentService.findDocumentByExternalId(TEST_EXTERNAL_ID))
        .thenReturn(Optional.of(TEST_DOC_ID));

    final var bytes = TEST_CONTENT.getBytes();
    final var contentDisposition = FormDataContentDisposition
        .name("contents")
        .fileName(TEST_FILENAME)
        .size(bytes.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(contentDisposition, bytes, APPLICATION_OCTET_STREAM_TYPE));
    final var entity = Entity.entity(multiPart, multiPart.getMediaType());

    var response = resource
        .client()
        .register(MultiPartFeature.class)
        .target("/documents/external-id/" + TEST_EXTERNAL_ID + "/" + TEST_TYPE)
        .request()
        .put(entity);

    assertThat(response.getStatus()).isEqualTo(200);
    var externalId = ArgumentCaptor.forClass(String.class);
    verify(documentService).findDocumentByExternalId(externalId.capture());
    assertThat(externalId.getValue()).isEqualTo(TEST_EXTERNAL_ID);
  }

  @Test
  public void updateDocumentByExternalIdAndType_shouldReturn404_whenExternalIdNotFound() {
    when(documentService.findDocumentByExternalId(TEST_EXTERNAL_ID))
        .thenReturn(Optional.empty());

    final var bytes = TEST_CONTENT.getBytes();
    final var contentDisposition = FormDataContentDisposition
        .name("contents")
        .fileName(TEST_FILENAME)
        .size(bytes.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(contentDisposition, bytes, APPLICATION_OCTET_STREAM_TYPE));
    final var entity = Entity.entity(multiPart, multiPart.getMediaType());

    var response = resource
        .client()
        .register(MultiPartFeature.class)
        .target("/documents/external-id/" + TEST_EXTERNAL_ID + "/" + TEST_TYPE)
        .request()
        .put(entity);

    assertThat(response.getStatus()).isEqualTo(404);
    var body = response.readEntity(String.class);
    String message = JsonPath.parse(body).read("$.message");
    assertThat(message).isEqualTo("Document with external id [" + TEST_EXTERNAL_ID + "] could not be found");
  }

  private Response postTestFile(String byExternalId) {
    final var bytes = TEST_CONTENT.getBytes();
    final var contentDisposition = FormDataContentDisposition
        .name("contents")
        .fileName(TEST_FILENAME)
        .size(bytes.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .field("type", TEST_TYPE)
        .field("byExternalId", byExternalId)
        .field("externalId", TEST_EXTERNAL_ID)
        .bodyPart(new FormDataBodyPart(contentDisposition, bytes, APPLICATION_OCTET_STREAM_TYPE));
    final var entity = Entity.entity(multiPart, multiPart.getMediaType());

    final var request = resource
        .client()
        .register(MultiPartFeature.class)
        .target("/documents")
        .request();
    return request.post(entity);
  }

}
