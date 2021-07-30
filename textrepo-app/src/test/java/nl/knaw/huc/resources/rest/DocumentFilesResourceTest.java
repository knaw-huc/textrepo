package nl.knaw.huc.resources.rest;

import com.jayway.jsonpath.JsonPath;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.config.PaginationConfiguration;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.DocumentsDao;
import nl.knaw.huc.helpers.Paginator;
import nl.knaw.huc.service.datetime.LocalDateTimeParamConverterProvider;
import nl.knaw.huc.service.document.files.DocumentFilesService;
import nl.knaw.huc.service.document.files.JdbiDocumentFilesService;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DocumentFilesResourceTest {

  private static final UUID documentUuid = UUID.fromString("0defaced-cafe-babe-dada-deadbeefc2c6");
  private static final Document document = new Document(documentUuid, "external-id");

  private static final Jdbi jdbi = mock(Jdbi.class);

  private static final int TEST_LIMIT = 10;
  private static final int TEST_OFFSET = 0;
  private static final Paginator paginator = createPaginator();

  private static Paginator createPaginator() {
    var config = new PaginationConfiguration();
    config.defaultOffset = TEST_OFFSET;
    config.defaultLimit = TEST_LIMIT;
    return new Paginator(config);
  }

  private static final DocumentFilesService documentFilesService = new JdbiDocumentFilesService(jdbi);

  // Don't forget to setup and reset mocks:
  private static final DocumentsDao DOCUMENTS_DAO = mock(DocumentsDao.class);
  private static final DocumentFilesDao DOCUMENT_FILES_DAO = mock(DocumentFilesDao.class);

  // With milliseconds:
  private static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS";

  public static final ResourceExtension resource;

  static {

    resource = ResourceExtension
        .builder()
        .addProvider(MultiPartFeature.class)
        .addProvider(() -> new LocalDateTimeParamConverterProvider(dateFormat))
        .addResource(new DocumentFilesResource(documentFilesService, paginator))
        .build();
  }

  @BeforeEach
  public void setupMocks() {
    when(jdbi.onDemand(DocumentsDao.class)).thenReturn(DOCUMENTS_DAO);
    when(jdbi.onDemand(DocumentFilesDao.class)).thenReturn(DOCUMENT_FILES_DAO);
    MockitoAnnotations.initMocks(this);
  }

  @AfterEach
  public void resetMocks() {
    reset(jdbi, DOCUMENT_FILES_DAO, DOCUMENTS_DAO);
  }

  @Test
  public void testGetDocumentFiles_returnsFilesPage() {
    List<TextRepoFile> files = new ArrayList<>();
    var file1 = new TextRepoFile(UUID.randomUUID(), (short) 1);
    files.add(file1);
    var file2 = new TextRepoFile(UUID.randomUUID(), (short) 2);
    files.add(file2);
    when(DOCUMENT_FILES_DAO.findFilesByDocumentAndTypeId(any(), isNull(), any())).thenReturn(files);
    when(DOCUMENT_FILES_DAO.countByDocumentAndTypeId(any(), isNull())).thenReturn(2L);

    var response = resource
        .client()
        .target("/rest/documents/" + documentUuid.toString() + "/files")
        .request()
        .get();
    verify(DOCUMENT_FILES_DAO, times(1)).findFilesByDocumentAndTypeId(eq(documentUuid), isNull(), any());
    var actual = JsonPath.parse(response.readEntity(String.class));
    assertThat(actual.read("$.items.length()", Integer.class)).isEqualTo(2);
    assertThat(actual.read("$.total", Integer.class)).isEqualTo(2);
    assertThat(actual.read("$.items[0].docId", String.class)).isEqualTo(documentUuid.toString());
    assertThat(actual.read("$.items[0].id", String.class)).isEqualTo(file1.getId().toString());
    assertThat(actual.read("$.items[1].docId", String.class)).isEqualTo(documentUuid.toString());
    assertThat(actual.read("$.items[1].id", String.class)).isEqualTo(file2.getId().toString());
  }

  @Test
  public void testGetDocumentFiles_returns404() {
    when(DOCUMENT_FILES_DAO.findFilesByDocumentAndTypeId(any(), isNull(), any())).thenReturn(new ArrayList<>());
    when(DOCUMENT_FILES_DAO.countByDocumentAndTypeId(any(), isNull())).thenReturn(0L);
    when(DOCUMENTS_DAO.get(any())).thenReturn(Optional.empty());

    var response = resource
        .client()
        .target("/rest/documents/" + documentUuid.toString() + "/files")
        .request()
        .get();

    verify(DOCUMENT_FILES_DAO, times(0)).findFilesByDocumentAndTypeId(eq(documentUuid), isNull(), any());
    var actual = JsonPath.parse(response.readEntity(String.class));
    assertThat(response.getStatus()).isEqualTo(404);
    assertThat(actual.read("$.message", String.class)).contains(documentUuid.toString());
  }

  @Test
  public void testGetDocumentFiles_filtersByType() {
    List<TextRepoFile> files = new ArrayList<>();
    var file1 = new TextRepoFile(UUID.randomUUID(), (short) 1);
    files.add(file1);
    when(DOCUMENT_FILES_DAO.findFilesByDocumentAndTypeId(any(), eq((short) 1), any())).thenReturn(files);
    when(DOCUMENT_FILES_DAO.countByDocumentAndTypeId(any(), eq((short) 1))).thenReturn(1L);

    var response = resource
        .client()
        .target("/rest/documents/" + documentUuid.toString() + "/files?typeId=1")
        .request()
        .get();

    verify(DOCUMENT_FILES_DAO, times(1)).findFilesByDocumentAndTypeId(
        eq(documentUuid),
        eq((short) 1),
        any()
    );

    var actual = JsonPath.parse(response.readEntity(String.class));
    assertThat(actual.read("$.items.length()", Integer.class)).isEqualTo(1);
    assertThat(actual.read("$.total", Integer.class)).isEqualTo(1);
    assertThat(actual.read("$.items[0].docId", String.class)).isEqualTo(documentUuid.toString());
    assertThat(actual.read("$.items[0].id", String.class)).isEqualTo(file1.getId().toString());
  }


}
