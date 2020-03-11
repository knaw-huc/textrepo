package nl.knaw.huc.resources.rest;

import ch.qos.logback.classic.Level;
import com.jayway.jsonpath.JsonPath;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.PaginationConfiguration;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.exceptions.MethodNotAllowedExceptionMapper;
import nl.knaw.huc.service.DocumentService;
import nl.knaw.huc.service.FileMetadataService;
import nl.knaw.huc.service.FileService;
import nl.knaw.huc.service.Paginator;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DocumentsResourceTest {

  private static final String TEST_EXTERNAL_ID = "test-external-id";

  private static final Jdbi jdbi = mock(Jdbi.class);
  private static final DocumentService documentService = mock(DocumentService.class);
  private static final FileService fileService = mock(FileService.class);

  @Captor
  private ArgumentCaptor<PageParams> pageParamsCaptor;

  // https://stackoverflow.com/questions/31730571/how-to-turn-on-tracing-in-a-unit-test-using-a-ResourceExtension
  // Actually has to go /before/ ResourceExtension these days as bootstrap()ing guarded against multiple calls.
  static {
    BootstrapLogging.bootstrap(Level.DEBUG);
  }

  private static final int TEST_LIMIT = 10;
  private static final int TEST_OFFSET = 0;
  private static final Paginator paginator = createPaginator();
  private static Paginator createPaginator() {
    var config = new PaginationConfiguration();
    config.defaultOffset = TEST_OFFSET;
    config.defaultLimit = TEST_LIMIT;
    return new Paginator(config);
  }

  public static final ResourceExtension resource = ResourceExtension
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new DocumentsResource(documentService, paginator))
      .addResource(new MethodNotAllowedExceptionMapper())
      .build();

  @BeforeEach
  public void setupMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @AfterEach
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

  @Test
  public void getDocuments_createsAndReturnsPage() {
    var offset = 2;
    var limit = 7;
    var total = 12;
    when(documentService.getAll(isNull(), any(PageParams.class)))
        .thenReturn(new Page<>(new ArrayList<>(), total, new PageParams(limit, offset)));

    // Request:
    var response = resource
        .client()
        .register(MultiPartFeature.class)
        .target("/rest/documents?offset=" + offset + "&limit=" + limit)
        .request().get();

    // Check response:
    assertThat(response.getStatus()).isEqualTo(200);
    var body = JsonPath.parse(response.readEntity(String.class));
    assertThat(body.read("$.items.length()", Integer.class)).isEqualTo(0);
    assertThat(body.read("$.page.offset", Integer.class)).isEqualTo(offset);
    assertThat(body.read("$.page.limit", Integer.class)).isEqualTo(limit);
    assertThat(body.read("$.total", Integer.class)).isEqualTo(total);

    // Check service call:
    verify(documentService, times(1)).getAll(
        isNull(),
        pageParamsCaptor.capture()
    );
    var pageParams = pageParamsCaptor.getValue();
    assertThat(pageParams.getLimit()).isEqualTo(limit);
    assertThat(pageParams.getOffset()).isEqualTo(offset);
  }

}
