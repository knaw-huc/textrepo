package nl.knaw.huc.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.db.VersionDao;
import nl.knaw.huc.service.DocumentFileService;
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
import java.io.IOException;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.resources.TestUtils.getResourceFileBits;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DocumentFilesResourceTest {
  private static final String content = "hello test";
  private String filename = "just-a-filename.txt";

  private static final Jdbi jdbi = mock(Jdbi.class);
  private static final FileService fileService = new FileService(mock(FileStorage.class));
  private static final ElasticDocumentIndexer documentIndexer = mock(ElasticDocumentIndexer.class);
  private static final MetadataService metadataService = mock(MetadataService.class);

  private static final VersionService versionService = new JdbiVersionService(
      jdbi, fileService,
      documentIndexer
  );

  private static final DocumentFileService documentFileService = new DocumentFileService(
      fileService,
      versionService,
      metadataService
  );

  private static final VersionDao versionDao = mock(VersionDao.class);

  @ClassRule
  public static final ResourceTestRule resource = ResourceTestRule
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new DocumentFilesResource(documentFileService))
      .build();

  @Captor
  private ArgumentCaptor<UUID> uuidCaptor;

  @Captor
  private ArgumentCaptor<MetadataEntry> metadataEntryCaptor;

  @Before
  public void setupMocks() {
    when(jdbi.onDemand(any())).thenReturn(versionDao);
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void resetMocks() {
    reset(jdbi, versionDao, documentIndexer, metadataService);
  }

  @Test
  public void testUpdateDocumentFile_addsFilenameMetadata() throws IOException {
    putTestFile();

    verify(metadataService, times(1)).update(
        uuidCaptor.capture(),
        metadataEntryCaptor.capture()
    );

    var metadataEntry = this.metadataEntryCaptor.getValue();
    assertThat(metadataEntry.getKey()).isEqualTo("filename");
    assertThat(metadataEntry.getValue()).isEqualTo(filename);
  }

  @Test
  public void testUpdateDocumentFile_addsFilenameMetadata_whenZip() throws IOException {
    var zipFilename = "multiple-hello-tests.zip";
    var zipFile = getResourceFileBits("zip/" + zipFilename);

    putTestFile(zipFile, zipFilename);

    verify(metadataService, times(2)).update(
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

  private void putTestFile() {
    var bytes = content.getBytes();
    putTestFile(bytes, filename);
  }

  private void putTestFile(byte[] bytes, String filename) {
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
        .target("/documents/b59c2b24-cafe-babe-9bb3-deadbeefc2c6/files")
        .request();

    final var entity = Entity.entity(multiPart, multiPart.getMediaType());

    request.put(entity);
  }


}
