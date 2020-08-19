package nl.knaw.huc.resources.rest;

import com.jayway.jsonpath.JsonPath;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.PaginationConfiguration;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.VersionsDao;
import nl.knaw.huc.helpers.Paginator;
import nl.knaw.huc.service.ContentsService;
import nl.knaw.huc.service.JdbiVersionService;
import nl.knaw.huc.service.VersionService;
import nl.knaw.huc.service.datetime.LocalDateTimeParamConverterProvider;
import nl.knaw.huc.service.index.MappedIndexer;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class FileVersionsResourceTest {

  private static final UUID uuid = UUID.fromString("0defaced-cafe-babe-dada-deadbeefc2c6");

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

  private static final VersionService versionService = new JdbiVersionService(
      jdbi,
      mock(ContentsService.class),
      newArrayList(mock(MappedIndexer.class)),
      UUID::randomUUID
  );

  private static final VersionsDao VERSIONS_DAO = mock(VersionsDao.class);

  // with milliseconds:
  private static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS";

  public static final ResourceExtension resource;

  static {

    resource = ResourceExtension
        .builder()
        .addProvider(MultiPartFeature.class)
        .addProvider(() -> new LocalDateTimeParamConverterProvider(dateFormat))
        .addResource(new FileVersionsResource(versionService, paginator))
        .build();
  }

  @BeforeEach
  public void setupMocks() {
    when(jdbi.onDemand(any())).thenReturn(VERSIONS_DAO);
    MockitoAnnotations.initMocks(this);
  }

  @AfterEach
  public void resetMocks() {
    reset(jdbi, VERSIONS_DAO);
  }

  @Test
  public void testGetVersions_returnsVersions() {
    List<Version> versions = new ArrayList<>();
    var sha1 = "fcd01d3b5648843931feb9ef4468250ac1a968a41add37f663af3bb0";
    var version1 = new Version(UUID.randomUUID(), uuid, sha1, LocalDateTime.now());
    versions.add(version1);
    var sha2 = "d476f2f6e00deaa918dfbec79545412134e02095f870269175b89376";
    var version2 = new Version(UUID.randomUUID(), uuid, sha2, LocalDateTime.now());
    versions.add(version2);
    when(VERSIONS_DAO.findByFileId(any(), any(PageParams.class), any())).thenReturn(versions);

    var response = resource
        .client()
        .target("/rest/files/" + uuid.toString() + "/versions")
        .request()
        .get();

    verify(VERSIONS_DAO, times(1)).findByFileId(eq(uuid), any(PageParams.class), any());
    var actual = JsonPath.parse(response.readEntity(String.class));
    assertThat(actual.read("$.items.length()", Integer.class)).isEqualTo(2);
    assertThat(actual.read("$.items[0].fileId", String.class)).isEqualTo(uuid.toString());
    assertThat(actual.read("$.items[0].contentsSha", String.class)).isEqualTo(sha1);
    assertThat(actual.read("$.items[1].fileId", String.class)).isEqualTo(uuid.toString());
    assertThat(actual.read("$.items[1].contentsSha", String.class)).isEqualTo(sha2);
  }

  @Test
  public void testGetVersions_returnsVersions_whenFilteredByCreatedAfter() {
    var past = new SimpleDateFormat(dateFormat).format(new Date());
    List<Version> versions = new ArrayList<>();
    var sha1 = "fcd01d3b5648843931feb9ef4468250ac1a968a41add37f663af3bb0";
    var version1 = new Version(UUID.randomUUID(), uuid, sha1, LocalDateTime.now());
    versions.add(version1);
    var sha2 = "d476f2f6e00deaa918dfbec79545412134e02095f870269175b89376";
    var version2 = new Version(UUID.randomUUID(), uuid, sha2, LocalDateTime.now());
    versions.add(version2);
    when(VERSIONS_DAO.findByFileId(any(), any(PageParams.class), any())).thenReturn(versions);

    var response = resource
        .client()
        .target("/rest/files/" + uuid.toString() + "/versions?createdAfter=" + past)
        .request()
        .get();

    verify(VERSIONS_DAO, times(1)).findByFileId(eq(uuid), any(PageParams.class), any());
    var actual = JsonPath.parse(response.readEntity(String.class));
    assertThat(actual.read("$.items.length()", Integer.class)).isEqualTo(2);
    assertThat(actual.read("$.items[0].fileId", String.class)).isEqualTo(uuid.toString());
    assertThat(actual.read("$.items[0].contentsSha", String.class)).isEqualTo(sha1);
    assertThat(actual.read("$.items[1].fileId", String.class)).isEqualTo(uuid.toString());
    assertThat(actual.read("$.items[1].contentsSha", String.class)).isEqualTo(sha2);
  }

  @Test
  public void testGetVersions_returnsMessageAboutDateFormat_whenWrongDateFormat() throws InterruptedException {
    var noSAndMs = "yyyy-MM-dd'T'HH:mm";
    var betweenVersions = new SimpleDateFormat(noSAndMs).format(new Date());
    var response = resource
        .client()
        .target("/rest/files/" + uuid.toString() + "/versions?createdAfter=" + betweenVersions)
        .request()
        .get();

    var actual = JsonPath.parse(response.readEntity(String.class));
    assertThat(actual.read("$.message", String.class)).contains(dateFormat);
  }

}
