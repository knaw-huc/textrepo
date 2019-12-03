package nl.knaw.huc.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.MultipleLocations;
import nl.knaw.huc.db.VersionDao;
import nl.knaw.huc.service.FileService;
import nl.knaw.huc.service.ContentsService;
import nl.knaw.huc.service.JdbiVersionService;
import nl.knaw.huc.service.MetadataService;
import nl.knaw.huc.service.VersionService;
import nl.knaw.huc.service.index.ElasticCustomFacetIndexer;
import nl.knaw.huc.service.index.ElasticFileIndexer;
import nl.knaw.huc.service.store.ContentsStorage;
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

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.resources.TestUtils.getResourceAsBytes;
import static nl.knaw.huc.resources.TestUtils.getResourceAsString;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FilesResourceTest {
  private static final String uuid = "b59c2b24-cafe-babe-9bb3-deadbeefc2c6";
  private static final String content = "hello test";
  private String filename = "just-a-filename.txt";

  private static final ContentsService contentsService = new ContentsService(mock(ContentsStorage.class));
  private static final Jdbi jdbi = mock(Jdbi.class);
  private static final ElasticFileIndexer fileIndexer = mock(ElasticFileIndexer.class);
  private static final MetadataService metadataService = mock(MetadataService.class);
  private static final ElasticCustomFacetIndexer facetIndexer = mock(ElasticCustomFacetIndexer.class);
  private static final VersionService versions = new JdbiVersionService(jdbi, contentsService, fileIndexer, newArrayList(facetIndexer));
  @SuppressWarnings("unchecked")
  private static final Supplier<UUID> idGenerator = mock(Supplier.class);
  private static final FileService FILE_SERVICE = new FileService(, versions, idGenerator, metadataService, );
  private static final VersionDao versionDao = mock(VersionDao.class);

  @ClassRule
  public static final ResourceTestRule resource = ResourceTestRule
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new FilesResource(FILE_SERVICE))
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
    reset(jdbi, versionDao, fileIndexer, metadataService);
  }

  @Test
  public void testPostFile_returns201CreatedWithLocationHeader_whenContentsUploaded() {
    final var response = postTestContents();
    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeaderString("Location")).endsWith("files/" + uuid);
  }

  @Test
  public void testAddFile_addsContentsWithFileIdToIndex() {
    postTestContents();
    var fileId = ArgumentCaptor.forClass(UUID.class);
    var latestVersionContent = ArgumentCaptor.forClass(String.class);
    verify(fileIndexer).indexFile(fileId.capture(), latestVersionContent.capture());
    assertThat(fileId.getValue()).isOfAnyClassIn(UUID.class);
    assertThat(latestVersionContent.getValue()).isEqualTo(content);
  }

  @Test
  public void testAddFile_addsZippedContents_whenZip() throws IOException {
    var zipFilename = "hello-test.zip";
    var zipFile = getResourceAsBytes("zip/" + zipFilename);

    postTestContents(zipFile, zipFilename);

    var zippedFile = ArgumentCaptor.forClass(String.class);
    verify(fileIndexer).indexFile(ArgumentCaptor.forClass(UUID.class).capture(), zippedFile.capture());
    assertThat(zippedFile.getValue()).isEqualToIgnoringWhitespace(content);
  }

  @Test
  public void testAddFile_addsMultipleFilesToIndex_whenZip() throws IOException {
    var zipFilename = "multiple-hello-tests.zip";
    var zipFile = getResourceAsBytes("zip/" + zipFilename);

    postTestContents(zipFile, zipFilename);

    verify(fileIndexer, times(2)).indexFile(any(UUID.class), any(String.class));
  }

  @Test
  public void testAddFile_returnsLocationsByFile_whenZip() throws IOException {
    var zipFilename = "multiple-hello-tests.zip";
    var zipFile = getResourceAsBytes("zip/" + zipFilename);

    var response = postTestContents(zipFile, zipFilename);
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
  public void testAddFile_skipsZippedDirectories_whenZip() throws IOException {
    var zipFilename = "hello-test-in-dir.zip";
    var zipFile = getResourceAsBytes("zip/" + zipFilename);

    postTestContents(zipFile, zipFilename);

    var zippedFile = ArgumentCaptor.forClass(String.class);
    verify(fileIndexer, times(1)).indexFile(
        ArgumentCaptor.forClass(UUID.class).capture(),
        zippedFile.capture()
    );
    assertThat(zippedFile.getValue()).isEqualToIgnoringWhitespace(content);
  }

  @Test
  public void testAddFile_skipsHiddenFiles_whenZip() throws IOException {
    var zipFilename = "mac-archive.zip";
    var zipFile = getResourceAsBytes("zip/" + zipFilename);

    postTestContents(zipFile, zipFilename);

    var zippedContentsCaptor = ArgumentCaptor.forClass(String.class);
    var zippedContent = getResourceAsString("zip/mac-archive-content.xml");
    verify(fileIndexer, times(1)).indexFile(
        ArgumentCaptor.forClass(UUID.class).capture(),
        zippedContentsCaptor.capture()
    );
    assertThat(zippedContentsCaptor.getValue()).isEqualToIgnoringWhitespace(zippedContent);
  }

  @Test
  public void testAddFile_addsFilenameMetadata() throws IOException {
    postTestContents();

    verify(metadataService, times(1)).insert(
        uuidCaptor.capture(),
        metadataEntryCaptor.capture()
    );

    var metadataEntry = this.metadataEntryCaptor.getValue();
    assertThat(metadataEntry.getKey()).isEqualTo("filename");
    assertThat(metadataEntry.getValue()).isEqualTo(filename);
  }

  @Test
  public void testAddFile_addsFilenameMetadata_whenZip() throws IOException {
    var zipFilename = "multiple-hello-tests.zip";
    var zipFile = getResourceAsBytes("zip/" + zipFilename);

    postTestContents(zipFile, zipFilename);

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

  private Response postTestContents() {
    var bytes = content.getBytes();
    return postTestContents(bytes, filename);
  }

  private Response postTestContents(byte[] bytes, String filename) {
    var contentDisposition = FormDataContentDisposition
        .name("contents")
        .fileName(filename)
        .size(bytes.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(contentDisposition, bytes, APPLICATION_OCTET_STREAM_TYPE));

    final var request = resource
        .client()
        .register(MultiPartFeature.class)
        .target("/files")
        .request();

    final var entity = Entity.entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }


}
