package nl.knaw.huc.resources.rest;

import com.jayway.jsonpath.JsonPath;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.config.PaginationConfiguration;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.VersionsDao;
import nl.knaw.huc.helpers.Paginator;
import nl.knaw.huc.service.contents.ContentsService;
import nl.knaw.huc.service.datetime.LocalDateTimeParamConverterProvider;
import nl.knaw.huc.service.index.IndexService;
import nl.knaw.huc.service.version.JdbiVersionService;
import nl.knaw.huc.service.version.VersionService;
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
import java.util.Optional;
import java.util.UUID;

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

  private static final UUID fileUuid = UUID.fromString("0defaced-cafe-babe-dada-deadbeefc2c6");
  private static final TextRepoFile file = new TextRepoFile(fileUuid, (short) 1);

  private static final Jdbi JDBI = mock(Jdbi.class);

  private static final int TEST_LIMIT = 10;
  private static final int TEST_OFFSET = 0;
  private static final Paginator paginator = createPaginator();

  private static Paginator createPaginator() {
    var config = new PaginationConfiguration();
    config.defaultOffset = TEST_OFFSET;
    config.defaultLimit = TEST_LIMIT;
    return new Paginator(config);
  }

  // Don't forget to setup and reset mocks:
  private static final VersionsDao VERSIONS_DAO = mock(VersionsDao.class);
  private static final FilesDao FILES_DAO = mock(FilesDao.class);
  private static final IndexService INDEX_SERVICE = mock(IndexService.class);

  private static final VersionService versionService = new JdbiVersionService(
      JDBI,
      mock(ContentsService.class),
      UUID::randomUUID,
      INDEX_SERVICE
  );

  // With milliseconds:
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
    when(JDBI.onDemand(VersionsDao.class)).thenReturn(VERSIONS_DAO);
    when(JDBI.onDemand(FilesDao.class)).thenReturn(FILES_DAO);
    MockitoAnnotations.initMocks(this);
  }

  @AfterEach
  public void resetMocks() {
    reset(JDBI, INDEX_SERVICE, VERSIONS_DAO, FILES_DAO);
  }

  @Test
  public void testGetVersions_returnsVersions() {
    List<Version> versions = new ArrayList<>();
    var sha1 = "fcd01d3b5648843931feb9ef4468250ac1a968a41add37f663af3bb0";
    var version1 = new Version(UUID.randomUUID(), fileUuid, sha1, LocalDateTime.now());
    versions.add(version1);
    var sha2 = "d476f2f6e00deaa918dfbec79545412134e02095f870269175b89376";
    var version2 = new Version(UUID.randomUUID(), fileUuid, sha2, LocalDateTime.now());
    versions.add(version2);
    when(VERSIONS_DAO.findByFileId(any(), any(PageParams.class), any())).thenReturn(versions);
    when(VERSIONS_DAO.countByFileId(any(), any())).thenReturn(2L);

    var response = resource
        .client()
        .target("/rest/files/" + fileUuid.toString() + "/versions")
        .request()
        .get();

    verify(VERSIONS_DAO, times(1)).findByFileId(eq(fileUuid), any(PageParams.class), any());
    var actual = JsonPath.parse(response.readEntity(String.class));
    assertThat(actual.read("$.items.length()", Integer.class)).isEqualTo(2);
    assertThat(actual.read("$.total", Integer.class)).isEqualTo(2);
    assertThat(actual.read("$.items[0].fileId", String.class)).isEqualTo(fileUuid.toString());
    assertThat(actual.read("$.items[0].contentsSha", String.class)).isEqualTo(sha1);
    assertThat(actual.read("$.items[1].fileId", String.class)).isEqualTo(fileUuid.toString());
    assertThat(actual.read("$.items[1].contentsSha", String.class)).isEqualTo(sha2);
  }

  @Test
  public void testGetVersions_returns404() {
    when(VERSIONS_DAO.find(any())).thenReturn(Optional.empty());
    when(VERSIONS_DAO.countByFileId(any(), any())).thenReturn(0L);
    when(FILES_DAO.find(any())).thenReturn(Optional.empty());

    var response = resource
        .client()
        .target("/rest/files/" + fileUuid.toString() + "/versions")
        .request()
        .get();

    verify(VERSIONS_DAO, times(0)).findByFileId(eq(fileUuid), any(PageParams.class), any());
    assertThat(response.getStatus()).isEqualTo(404);
    var actual = JsonPath.parse(response.readEntity(String.class));
    assertThat(actual.read("$.message", String.class)).contains(fileUuid.toString());
  }

  @Test
  public void testGetVersions_returnsVersions_whenFilteredByCreatedAfter() {
    var past = new SimpleDateFormat(dateFormat).format(new Date());
    List<Version> versions = new ArrayList<>();
    var sha1 = "fcd01d3b5648843931feb9ef4468250ac1a968a41add37f663af3bb0";
    var version1 = new Version(UUID.randomUUID(), fileUuid, sha1, LocalDateTime.now());
    versions.add(version1);
    var sha2 = "d476f2f6e00deaa918dfbec79545412134e02095f870269175b89376";
    var version2 = new Version(UUID.randomUUID(), fileUuid, sha2, LocalDateTime.now());
    versions.add(version2);
    when(VERSIONS_DAO.findByFileId(any(), any(PageParams.class), any())).thenReturn(versions);
    when(VERSIONS_DAO.countByFileId(any(), any())).thenReturn(2L);

    var response = resource
        .client()
        .target("/rest/files/" + fileUuid.toString() + "/versions?createdAfter=" + past)
        .request()
        .get();

    verify(VERSIONS_DAO, times(1)).findByFileId(eq(fileUuid), any(PageParams.class), any());
    var actual = JsonPath.parse(response.readEntity(String.class));
    assertThat(actual.read("$.items.length()", Integer.class)).isEqualTo(2);
    assertThat(actual.read("$.items[0].fileId", String.class)).isEqualTo(fileUuid.toString());
    assertThat(actual.read("$.items[0].contentsSha", String.class)).isEqualTo(sha1);
    assertThat(actual.read("$.items[1].fileId", String.class)).isEqualTo(fileUuid.toString());
    assertThat(actual.read("$.items[1].contentsSha", String.class)).isEqualTo(sha2);
  }

  @Test
  public void testGetVersions_returnsMessageAboutDateFormat_whenWrongDateFormat() {
    var noSAndMs = "yyyy-MM-dd'T'HH:mm";
    var betweenVersions = new SimpleDateFormat(noSAndMs).format(new Date());
    var response = resource
        .client()
        .target("/rest/files/" + fileUuid.toString() + "/versions?createdAfter=" + betweenVersions)
        .request()
        .get();

    var actual = JsonPath.parse(response.readEntity(String.class));
    assertThat(actual.read("$.message", String.class)).contains(dateFormat);
  }

}
