package nl.knaw.huc.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.MultipleLocations;
import nl.knaw.huc.db.VersionDao;
import nl.knaw.huc.service.DocumentService;
import nl.knaw.huc.service.FileService;
import nl.knaw.huc.service.JdbiVersionService;
import nl.knaw.huc.service.MetadataService;
import nl.knaw.huc.service.VersionService;
import nl.knaw.huc.service.index.ElasticDocumentIndexer;
import nl.knaw.huc.service.store.FileStorage;
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
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.resources.TestUtils.getResourceFileBits;
import static nl.knaw.huc.resources.TestUtils.getResourceFileString;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DocumentsResourceTest {
  private static final String uuid = "b59c2b24-cafe-babe-9bb3-deadbeefc2c6";
  private static final String content = "hello test";
  private String filename = "just-a-filename.txt";

  private static final FileService files = new FileService(mock(FileStorage.class));
  private static final Jdbi jdbi = mock(Jdbi.class);
  private static final ElasticDocumentIndexer documentIndexer = mock(ElasticDocumentIndexer.class);
  private static final MetadataService metadataService = mock(MetadataService.class);
  private static final VersionService versions = new JdbiVersionService(jdbi, files, documentIndexer);
  @SuppressWarnings("unchecked")
  private static final Supplier<UUID> idGenerator = mock(Supplier.class);
  private static final DocumentService documentService = new DocumentService(versions, idGenerator, metadataService);
  private static final VersionDao versionDao = mock(VersionDao.class);

  @ClassRule
  public static final ResourceTestRule resource = ResourceTestRule
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new DocumentsResource(documentService))
      .build();

  @Captor
  private ArgumentCaptor<UUID> uuidCaptor;

  @Captor
  private ArgumentCaptor<MetadataEntry> metadataEntryCaptor;

  @Before
  public void setupMocks() {
    when(jdbi.onDemand(any())).thenReturn(versionDao);
    when(idGenerator.get()).thenReturn(UUID.fromString(uuid));
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void resetMocks() {
    reset(jdbi, versionDao, documentIndexer, metadataService);
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
    var documentId = ArgumentCaptor.forClass(UUID.class);
    var latestVersionContent = ArgumentCaptor.forClass(String.class);
    verify(documentIndexer).indexDocument(documentId.capture(), latestVersionContent.capture());
    assertThat(documentId.getValue()).isOfAnyClassIn(UUID.class);
    assertThat(latestVersionContent.getValue()).isEqualTo(content);
  }

  @Test
  public void testAddDocument_addsZippedFile_whenZip() throws IOException {
    var zipFilename = "hello-test.zip";
    var zipFile = getResourceFileBits("zip/" + zipFilename);

    postTestFile(zipFile, zipFilename);

    var zippedFile = ArgumentCaptor.forClass(String.class);
    verify(documentIndexer).indexDocument(ArgumentCaptor.forClass(UUID.class).capture(), zippedFile.capture());
    assertThat(zippedFile.getValue()).isEqualToIgnoringWhitespace(content);
  }

  @Test
  public void testAddDocument_addsMultipleFilesToIndex_whenZip() throws IOException {
    var zipFilename = "multiple-hello-tests.zip";
    var zipFile = getResourceFileBits("zip/" + zipFilename);

    postTestFile(zipFile, zipFilename);

    verify(documentIndexer, times(2)).indexDocument(any(UUID.class), any(String.class));
  }

  @Test
  public void testAddDocument_returnsLocationsByFile_whenZip() throws IOException {
    var zipFilename = "multiple-hello-tests.zip";
    var zipFile = getResourceFileBits("zip/" + zipFilename);

    var response = postTestFile(zipFile, zipFilename);
    var body = response.readEntity(String.class);

    var locations = new ObjectMapper().readValue(body, MultipleLocations.class).locations;

    var file1 = locations.get("hello-test.txt");
    assertThat(file1).isNotNull();
    // check is valid uuid:
    UUID.fromString(file1.toString().split("/")[2]);

    var file2 = locations.get("hello-test2.txt");
    assertThat(file2).isNotNull();
    // check is valid uuid:
    UUID.fromString(file2.toString().split("/")[2]);
  }

  @Test
  public void testAddDocument_skipsZippedDirectories_whenZip() throws IOException {
    var zipFilename = "hello-test-in-dir.zip";
    var zipFile = getResourceFileBits("zip/" + zipFilename);

    postTestFile(zipFile, zipFilename);

    var zippedFile = ArgumentCaptor.forClass(String.class);
    verify(documentIndexer, times(1)).indexDocument(
        ArgumentCaptor.forClass(UUID.class).capture(),
        zippedFile.capture()
    );
    assertThat(zippedFile.getValue()).isEqualToIgnoringWhitespace(content);
  }

  @Test
  public void testAddDocument_skipsHiddenFiles_whenZip() throws IOException {
    var zipFilename = "mac-archive.zip";
    var zipFile = getResourceFileBits("zip/" + zipFilename);

    postTestFile(zipFile, zipFilename);

    var zippedFile = ArgumentCaptor.forClass(String.class);
    var zippedContent = getResourceFileString("zip/mac-archive-content.xml");
    verify(documentIndexer, times(1)).indexDocument(
        ArgumentCaptor.forClass(UUID.class).capture(),
        zippedFile.capture()
    );
    assertThat(zippedFile.getValue()).isEqualToIgnoringWhitespace(zippedContent);
  }

  @Test
  public void testAddDocument_addsFilenameMetadata() throws IOException {
    postTestFile();

    verify(metadataService, times(1)).insert(
        uuidCaptor.capture(),
        metadataEntryCaptor.capture()
    );

    var metadataEntry = this.metadataEntryCaptor.getValue();
    assertThat(metadataEntry.getKey()).isEqualTo("filename");
    assertThat(metadataEntry.getValue()).isEqualTo(filename);
  }

  @Test
  public void testAddDocument_addsFilenameMetadata_whenZip() throws IOException {
    var zipFilename = "multiple-hello-tests.zip";
    var zipFile = getResourceFileBits("zip/" + zipFilename);

    postTestFile(zipFile, zipFilename);

    verify(metadataService, times(2)).insert(
        uuidCaptor.capture(),
        metadataEntryCaptor.capture()
    );

    var metadataEntries = this.metadataEntryCaptor.getAllValues();
    assertThat(metadataEntries.size()).isEqualTo(2);
    assertThat(metadataEntries.get(0).getKey()).isEqualTo("filename");
    assertThat(metadataEntries.get(0).getValue()).isEqualTo("hello-test.txt");
    assertThat(metadataEntries.get(1).getKey()).isEqualTo("filename");
    assertThat(metadataEntries.get(1).getValue()).isEqualTo("hello-test2.txt");
  }

  private Response postTestFile() {
    var bytes = content.getBytes();
    return postTestFile(bytes, filename);
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
