package nl.knaw.huc.resources.dashboard;

import ch.qos.logback.classic.Level;
import com.jayway.jsonpath.JsonPath;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.PaginationConfiguration;
import nl.knaw.huc.core.DocumentsOverview;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.helpers.Paginator;
import nl.knaw.huc.service.dashboard.DashboardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
class DashboardResourceTest {
  private static final DashboardService DASHBOARD_SERVICE = mock(DashboardService.class);
  private static final int TEST_LIMIT = 10;
  private static final int TEST_OFFSET = 0;
  private static final Paginator paginator = createPaginator();

  private static Paginator createPaginator() {
    var config = new PaginationConfiguration();
    config.defaultOffset = TEST_OFFSET;
    config.defaultLimit = TEST_LIMIT;
    return new Paginator(config);
  }

  static {
    BootstrapLogging.bootstrap(Level.DEBUG);
  }

  public static final ResourceExtension resource = ResourceExtension
      .builder()
      .addResource(new DashboardResource(DASHBOARD_SERVICE, paginator))
      .build();

  @Captor
  private ArgumentCaptor<PageParams> pageParamsCaptor;

  @BeforeEach
  void setupMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @AfterEach
  void resetMocks() {
    reset(DASHBOARD_SERVICE);
  }

  @Test
  public void getStats_returnsProperDocumentsOverviewInJson() {
    final var expectedDocumentCount = 10;
    final var expectedHasFile = 8;
    final var expectedHasMetadata = 4;
    final var expectedHasBoth = 2;
    final var it = new DocumentsOverview(expectedDocumentCount, expectedHasFile, expectedHasMetadata, expectedHasBoth);

    when(DASHBOARD_SERVICE.getDocumentsOverview()).thenReturn(it);

    // Request:
    final var response = resource
        .client()
        .target("/dashboard")
        .request(APPLICATION_JSON)
        .get();

    // Check service call:
    verify(DASHBOARD_SERVICE, times(1)).getDocumentsOverview();

    // Check response:
    assertThat(response.getStatus()).isEqualTo(200);
    final var body = response.readEntity(String.class);
    final var json = JsonPath.parse(body);
    assertThat(json.read("$.documentCount", Integer.class)).isEqualTo(expectedDocumentCount);
    assertThat(json.read("$.hasFile", Integer.class)).isEqualTo(expectedHasFile);
    assertThat(json.read("$.hasMetadata", Integer.class)).isEqualTo(expectedHasMetadata);
    assertThat(json.read("$.hasBoth", Integer.class)).isEqualTo(expectedHasBoth);
  }

  @Test
  public void findOrphans_createsAndReturnsPage() {
    final var offset = 2;
    final var limit = 7;
    final var total = 12;
    when(DASHBOARD_SERVICE.findOrphans(any(PageParams.class)))
        .thenReturn(new Page<>(new ArrayList<>(), total, new PageParams(limit, offset)));

    // Request:
    final var response = resource
        .client()
        .target("/dashboard/orphans")
        .queryParam("limit", limit)
        .queryParam("offset", offset)
        .request(APPLICATION_JSON)
        .get();

    // Check service call:
    verify(DASHBOARD_SERVICE, times(1)).findOrphans(pageParamsCaptor.capture());
    final var page = pageParamsCaptor.getValue();
    assertThat(page.getLimit()).isEqualTo(limit);
    assertThat(page.getOffset()).isEqualTo(offset);

    // Check response:
    assertThat(response.getStatus()).isEqualTo(200);
    final var body = response.readEntity(String.class);
    final var json = JsonPath.parse(body);
    assertThat(json.read("$.items.length()", Integer.class)).isEqualTo(0);
    assertThat(json.read("$.page.offset", Integer.class)).isEqualTo(offset);
    assertThat(json.read("$.page.limit", Integer.class)).isEqualTo(limit);
    assertThat(json.read("$.total", Integer.class)).isEqualTo(total);
  }

  @Test
  public void countDocumentsByMetadataKey_ReturnsProperCounts() {
    final Map.Entry<String, Integer> entryA = Map.entry("a", 3);
    final Map.Entry<String, Integer> entryB = Map.entry("b", 14);
    final Map.Entry<String, Integer> entryC = Map.entry("<no metadata>", 1693);
    final Map<String, Integer> expectedKeyCounts = createCounts(entryA, entryB, entryC);
    when(DASHBOARD_SERVICE.countDocumentsByMetadataKey()).thenReturn(expectedKeyCounts);

    // Request:
    final var response = resource
        .client()
        .target("/dashboard/metadata")
        .request(APPLICATION_JSON)
        .get();

    // Check service call:
    verify(DASHBOARD_SERVICE, times(1)).countDocumentsByMetadataKey();

    // Check response:
    assertThat(response.getStatus()).isEqualTo(200);
    var body = response.readEntity(String.class);
    var json = JsonPath.parse(body);
    LinkedHashMap<String, Integer> actualCounts = json.read("$");
    assertThat(actualCounts).hasSameSizeAs(expectedKeyCounts);
    assertThat(actualCounts).containsExactly(entryA, entryB, entryC);
  }

  @Test
  public void countDocumentsByMetadataValue_ReturnsProperCounts() {
    final var testKey = "testKey";
    final Map.Entry<String, Integer> entryA = Map.entry("a", 3);
    final Map.Entry<String, Integer> entryB = Map.entry("b", 14);
    final Map.Entry<String, Integer> entryC = Map.entry("<no metadata>", 1693);
    final Map<String, Integer> expectedKeyCounts = createCounts(entryA, entryB, entryC);
    when(DASHBOARD_SERVICE.countDocumentsByMetadataValue(testKey)).thenReturn(expectedKeyCounts);

    // Request:
    final var response = resource
        .client()
        .target("/dashboard/metadata/{key}")
        .resolveTemplate("key", testKey)
        .request(APPLICATION_JSON)
        .get();

    // Check service call:
    verify(DASHBOARD_SERVICE, times(1)).countDocumentsByMetadataValue(testKey);

    // Check response:
    assertThat(response.getStatus()).isEqualTo(200);
    var body = response.readEntity(String.class);
    var json = JsonPath.parse(body);
    LinkedHashMap<String, Integer> actualCounts = json.read("$");
    assertThat(actualCounts).hasSameSizeAs(expectedKeyCounts);
    assertThat(actualCounts).containsExactly(entryA, entryB, entryC);
  }

  @SafeVarargs
  private static Map<String, Integer> createCounts(final Map.Entry<String, Integer>... entries) {
    final Map<String, Integer> counts = new LinkedHashMap<>(); // Map.of(...) does not preserve order
    for (final var e : entries) {
      counts.put(e.getKey(), e.getValue());
    }
    return counts;
  }

}