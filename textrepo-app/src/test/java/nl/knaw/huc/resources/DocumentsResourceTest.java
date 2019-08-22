package nl.knaw.huc.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huc.db.VersionDao;
import nl.knaw.huc.service.DocumentService;
import nl.knaw.huc.service.FileService;
import nl.knaw.huc.service.JdbiVersionService;
import nl.knaw.huc.service.MetadataService;
import nl.knaw.huc.service.VersionService;
import nl.knaw.huc.service.index.ElasticDocumentIndexer;
import nl.knaw.huc.service.store.FileStorage;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DocumentsResourceTest {
  private static final String uuid = "b59c2b24-cafe-babe-9bb3-deadbeefc2c6";
  private static final String content = "hello test";

  private static final FileService files = new FileService(mock(FileStorage.class));
  private static final Jdbi jdbi = mock(Jdbi.class);
  private static final ElasticDocumentIndexer documentIndexer = mock(ElasticDocumentIndexer.class);
  private static final VersionService versions = new JdbiVersionService(jdbi, files, documentIndexer, mock(MetadataService.class));
  @SuppressWarnings("unchecked")
  private static final Supplier<UUID> idGenerator = mock(Supplier.class);
  private static final DocumentService documentService = new DocumentService(versions, idGenerator);
  private static final VersionDao versionDao = mock(VersionDao.class);

  @ClassRule
  public static final ResourceTestRule resource = ResourceTestRule
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new DocumentsResource(documentService))
      .build();

  @Before
  public void setupMocks() {
    when(jdbi.onDemand(any())).thenReturn(versionDao);
    when(idGenerator.get()).thenReturn(UUID.fromString(uuid));
  }

  @After
  public void resetMocks() {
    reset(jdbi, versionDao, documentIndexer);
  }

  @Test
  public void testPostDocument_returns201CreatedWithLocationHeader_whenFileUploaded() {
    final var response = postTestFile();
    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeaderString("Location")).endsWith("documents/" + uuid);
  }

  @Test
  public void testAddDocument_addsFileWithDocumentIdToIndex() {
    postTestFile();
    var documentId = forClass(UUID.class);
    var latestVersionContent = forClass(String.class);
    verify(documentIndexer).indexDocument(documentId.capture(), latestVersionContent.capture());
    assertThat(documentId.getValue()).isOfAnyClassIn(UUID.class);
    assertThat(latestVersionContent.getValue()).isEqualTo(content);
  }

  @Test
  public void testAddDocument_addsZippedFile_whenZip() throws IOException {
    var zipFilename = "hello-test.zip";
    var zipFile = getResourceFileBits("zip/" + zipFilename);

    postTestFile(zipFile, zipFilename);

    var zippedFile = forClass(String.class);
    verify(documentIndexer).indexDocument(forClass(UUID.class).capture(), zippedFile.capture());
    assertThat(zippedFile.getValue()).isEqualToIgnoringWhitespace(content);
  }

  @Test
  public void testAddDocument_addsMultipleFiles_whenZip() throws IOException {
    var zipFilename = "multiple-hello-tests.zip";
    var zipFile = getResourceFileBits("zip/" + zipFilename);

    postTestFile(zipFile, zipFilename);

    verify(documentIndexer, times(2)).indexDocument(any(UUID.class), any(String.class));
  }

  @Test
  public void testAddDocument_skipsZippedDirectories_whenZip() throws IOException {
    var zipFilename = "hello-test-in-dir.zip";
    var zipFile = getResourceFileBits("zip/" + zipFilename);

    postTestFile(zipFile, zipFilename);

    var zippedFile = forClass(String.class);
    verify(documentIndexer, times(1)).indexDocument(forClass(UUID.class).capture(), zippedFile.capture());
    assertThat(zippedFile.getValue()).isEqualToIgnoringWhitespace(content);
  }

  @Test
  public void testAddDocument_skipsHiddenFiles_whenZip() throws IOException {
    var zipFilename = "mac-archive.zip";
    var zipFile = getResourceFileBits("zip/" + zipFilename);

    postTestFile(zipFile, zipFilename);

    var zippedFile = forClass(String.class);
    var zippedContent = getResourceFileString("zip/mac-archive-content.xml");
    verify(documentIndexer, times(1)).indexDocument(forClass(UUID.class).capture(), zippedFile.capture());
    assertThat(zippedFile.getValue()).isEqualToIgnoringWhitespace(zippedContent);
  }

  private byte[] getResourceFileBits(String resourcePath) throws IOException {
    return IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(resourcePath));
  }

  private String getResourceFileString(String resourcePath) throws IOException {
    return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resourcePath), UTF_8);
  }

  private Response postTestFile() {
    var bytes = content.getBytes();
    return postTestFile(bytes, "just-a-filename.txt");
  }

  private Response postTestFile(byte[] bytes, String filename) {
    var contentDisposition = FormDataContentDisposition
        .name("file")
        .fileName(filename)
        .size(bytes.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(contentDisposition, bytes, APPLICATION_OCTET_STREAM_TYPE));

    final var request = resource
        .client()
        .register(MultiPartFeature.class)
        .target("/documents")
        .request();

    final var entity = Entity.entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }


}
