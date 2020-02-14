package nl.knaw.huc.resources;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.VersionsDao;
import nl.knaw.huc.service.ContentsService;
import nl.knaw.huc.service.FileMetadataService;
import nl.knaw.huc.service.FileService;
import nl.knaw.huc.service.JdbiFileService;
import nl.knaw.huc.service.JdbiVersionService;
import nl.knaw.huc.service.TypeService;
import nl.knaw.huc.service.VersionService;
import nl.knaw.huc.service.index.ElasticCustomIndexer;
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
import org.junit.jupiter.api.extension.ExtendWith;
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
import static nl.knaw.huc.resources.ResourceTestUtils.responsePart;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class  DeprecatedFilesResourceTest {

  private static final String uuid = "b59c2b24-cafe-babe-9bb3-deadbeefc2c6";
  private static final String contents = "hello test";
  private String filename = "just-a-filename.txt";

  private static final ContentsService contentsService = new ContentsService(mock(ContentsStorage.class));
  private static final Jdbi jdbi = mock(Jdbi.class);
  private static final ElasticFileIndexer fileIndexer = mock(ElasticFileIndexer.class);
  private static final FileMetadataService FILE_METADATA_SERVICE = mock(FileMetadataService.class);
  private static final TypeService typeService = mock(TypeService.class);
  private static final ElasticCustomIndexer facetIndexer = mock(ElasticCustomIndexer.class);
  private static final VersionService versions =
      new JdbiVersionService(jdbi, contentsService, fileIndexer, newArrayList(facetIndexer), UUID::randomUUID);
  @SuppressWarnings("unchecked")
  private static final Supplier<UUID> idGenerator = mock(Supplier.class);
  private static final FileService FILE_SERVICE =
      new JdbiFileService(jdbi, typeService, versions, FILE_METADATA_SERVICE, idGenerator);
  private static final VersionsDao VERSIONS_DAO = mock(VersionsDao.class);
  private static final FilesDao FILES_DAO = mock(FilesDao.class);

  @ClassRule
  public static final ResourceExtension resource = ResourceExtension
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new DeprecatedFilesResource(FILE_SERVICE, contents.length()))
      .build();

  @Captor
  private ArgumentCaptor<UUID> uuidCaptor;

  @Captor
  private ArgumentCaptor<MetadataEntry> metadataEntryCaptor;

  @Before
  public void setupMocks() {
    when(jdbi.onDemand(FilesDao.class)).thenReturn(FILES_DAO);
    when(jdbi.onDemand(VersionsDao.class)).thenReturn(VERSIONS_DAO);
    when(idGenerator.get()).thenReturn(UUID.fromString(uuid));
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void resetMocks() {
    reset(jdbi, FILES_DAO, VERSIONS_DAO, fileIndexer, FILE_METADATA_SERVICE);
  }

  @Test
  public void testPostFile_returns201CreatedWithLocationHeader_whenContentsUploaded() {
    final var response = postTestContents();
    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeaderString("Location")).endsWith("files/" + uuid + "/latest");
  }

  @Test
  public void testAddFile_addsContentsWithFileIdToIndex() {
    postTestContents();
    var file = ArgumentCaptor.forClass(TextrepoFile.class);
    var latestVersionContents = ArgumentCaptor.forClass(String.class);
    verify(fileIndexer).indexFile(file.capture(), latestVersionContents.capture());
    assertThat(file.getValue()).isOfAnyClassIn(TextrepoFile.class);
    assertThat(latestVersionContents.getValue()).isEqualTo(contents);
  }

  @Test
  public void testAddFile_addsFilenameMetadata() {
    postTestContents();

    verify(FILE_METADATA_SERVICE, times(1)).insert(
        uuidCaptor.capture(),
        metadataEntryCaptor.capture()
    );

    var metadataEntry = this.metadataEntryCaptor.getValue();
    assertThat(metadataEntry.getKey()).isEqualTo("filename");
    assertThat(metadataEntry.getValue()).isEqualTo(filename);
  }

  @Test
  public void testPostFile_yieldsPayloadTooLarge_whenContentsLongerThanMaxAllowed() throws IOException {
    final var response = postTestContents((contents + " MADE TOO LONG").getBytes(), filename);
    assertThat(response.getStatus()).isEqualTo(413);

    final var message = responsePart(response, "message");
    assertThat(message).containsIgnoringCase("max. allowed size");
    assertThat(message).contains(String.valueOf(contents.length()));
  }

  private Response postTestContents() {
    var bytes = contents.getBytes();
    return postTestContents(bytes, filename);
  }

  private Response postTestContents(byte[] bytes, String filename) {
    var contentDisposition = FormDataContentDisposition
        .name("contents")
        .fileName(filename)
        .size(bytes.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .field("type", "text")
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
