package nl.knaw.huc.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.db.VersionDao;
import nl.knaw.huc.service.DocumentService;
import nl.knaw.huc.service.FileIndexService;
import nl.knaw.huc.service.FileService;
import nl.knaw.huc.service.FileStoreService;
import nl.knaw.huc.service.JdbiVersionService;
import nl.knaw.huc.service.VersionService;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DocumentsResourceTest {
  private static final String uuid = "b59c2b24-cafe-babe-9bb3-deadbeefc2c6";
  private static final String content = "hello test";

  private static final FileIndexService fileIndexService = mock(FileIndexService.class);
  private static final FileService files = new FileService(mock(FileStoreService.class), fileIndexService);
  private static final Jdbi jdbi = mock(Jdbi.class);
  private static final VersionService versions = new JdbiVersionService(jdbi, files);
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
    reset(jdbi, versionDao, fileIndexService);
  }

  @Test
  public void testPostDocument_returns201CreatedWithLocationHeader_whenFileUploaded() {
    final var response = postTestFile();
    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeaderString("Location")).endsWith("documents/" + uuid);
  }

  @Test
  public void testAddDocument_addsFileToIndex() {
    final var response = postTestFile();
    assertThat(response.getStatus()).isEqualTo(201);
    var argument = ArgumentCaptor.forClass(TextRepoFile.class);
    verify(fileIndexService).indexFile(argument.capture());
    assertThat(argument.getValue().getContent()).isEqualTo(content.getBytes());
  }

  private Response postTestFile() {
    final var multiPart = new FormDataMultiPart().field("file", content);

    final var request = resource
        .client()
        .register(MultiPartFeature.class)
        .target("/documents")
        .request();

    final var entity = Entity.entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }


}
