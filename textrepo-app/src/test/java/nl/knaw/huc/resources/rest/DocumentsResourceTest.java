package nl.knaw.huc.resources.rest;

import ch.qos.logback.classic.Level;
import com.jayway.jsonpath.JsonPath;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.PaginationConfiguration;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.exceptions.MethodNotAllowedExceptionMapper;
import nl.knaw.huc.helpers.Paginator;
import nl.knaw.huc.service.datetime.LocalDateTimeParamConverterProvider;
import nl.knaw.huc.service.document.DocumentService;
import nl.knaw.huc.service.file.FileService;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

  @Captor
  private ArgumentCaptor<LocalDateTime> createdAfterCaptor;

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

  private static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS";

  public static final ResourceExtension resource = ResourceExtension
      .builder()
      .addProvider(MultiPartFeature.class)
      .addProvider(() -> new LocalDateTimeParamConverterProvider(dateFormat))
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
    when(documentService.get(docId)).thenReturn(Optional.of(new Document(docId, TEST_EXTERNAL_ID, now())));
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
    when(documentService.getAll(isNull(), isNull(), any(PageParams.class)))
        .thenReturn(new Page<>(new ArrayList<>(), total, new PageParams(limit, offset)));

    // Request:
    var response = resource
        .client()
        .register(MultiPartFeature.class)
        .target("/rest/documents?offset=" + offset + "&limit=" + limit)
        .request().get();

    // Check service call:
    verify(documentService, times(1)).getAll(
        isNull(),
        any(),
        pageParamsCaptor.capture()
    );
    var pageParams = pageParamsCaptor.getValue();
    assertThat(pageParams.getLimit()).isEqualTo(limit);
    assertThat(pageParams.getOffset()).isEqualTo(offset);

    // Check response:
    assertThat(response.getStatus()).isEqualTo(200);
    var body = JsonPath.parse(response.readEntity(String.class));
    assertThat(body.read("$.items.length()", Integer.class)).isEqualTo(0);
    assertThat(body.read("$.page.offset", Integer.class)).isEqualTo(offset);
    assertThat(body.read("$.page.limit", Integer.class)).isEqualTo(limit);
    assertThat(body.read("$.total", Integer.class)).isEqualTo(total);

  }

  @Test
  public void getDocuments_createsAndReturnsPage_filtersByCreationDate() {
    var now = now();
    var expectedDateTime = now.format(DateTimeFormatter.ofPattern(dateFormat));

    var expectedTotal = 5;
    when(documentService.getAll(isNull(), any(LocalDateTime.class), any(PageParams.class)))
        .thenReturn(new Page<>(new ArrayList<>(), expectedTotal, new PageParams(TEST_LIMIT, TEST_OFFSET)));

    // Request:
    var response = resource
        .client()
        .register(MultiPartFeature.class)
        .target("/rest/documents?createdAfter=" + expectedDateTime)
        .request().get();

    // Check service call:
    verify(documentService, times(1)).getAll(
        isNull(),
        createdAfterCaptor.capture(),
        pageParamsCaptor.capture()
    );

    var pageParams = pageParamsCaptor.getValue();
    assertThat(pageParams.getLimit()).isEqualTo(TEST_LIMIT);
    assertThat(pageParams.getOffset()).isEqualTo(TEST_OFFSET);

    var createdAfter = createdAfterCaptor.getValue();
    var receivedDateTime = createdAfter.format(DateTimeFormatter.ofPattern(dateFormat));
    assertThat(receivedDateTime).isEqualTo(expectedDateTime);

    // Check response:
    assertThat(response.getStatus()).isEqualTo(200);
  }

}
