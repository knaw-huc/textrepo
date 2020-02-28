package nl.knaw.huc.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.VersionsDao;
import nl.knaw.huc.resources.rest.FileVersionsResource;
import nl.knaw.huc.service.ContentsService;
import nl.knaw.huc.service.JdbiVersionService;
import nl.knaw.huc.service.VersionService;
import nl.knaw.huc.service.index.ElasticCustomIndexer;
import nl.knaw.huc.service.index.ElasticFileIndexer;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class FileVersionsResourceTest {

  private static final UUID uuid = UUID.fromString("0defaced-cafe-babe-dada-deadbeefc2c6");

  private static final Jdbi jdbi = mock(Jdbi.class);

  private static final VersionService versionService = new JdbiVersionService(
      jdbi,
      mock(ContentsService.class),
      newArrayList(mock(ElasticCustomIndexer.class)),
      UUID::randomUUID
  );

  private static final VersionsDao VERSIONS_DAO = mock(VersionsDao.class);

  public static final ResourceExtension resource = ResourceExtension
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new FileVersionsResource(versionService))
      .build();

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
  public void testGetVersions_returnsVersions() throws IOException {
    List<Version> versions = new ArrayList<>();
    var sha1 = "fcd01d3b5648843931feb9ef4468250ac1a968a41add37f663af3bb0";
    var version1 = new Version(UUID.randomUUID(), uuid, LocalDateTime.now(), sha1);
    versions.add(version1);
    var sha2 = "d476f2f6e00deaa918dfbec79545412134e02095f870269175b89376";
    var version2 = new Version(UUID.randomUUID(), uuid, LocalDateTime.now(), sha2);
    versions.add(version2);
    when(VERSIONS_DAO.findByFileId(any())).thenReturn(versions);

    var response = getVersions(uuid);

    verify(VERSIONS_DAO, times(1)).findByFileId(uuid);
    var responseJson = response.readEntity(String.class);
    var mapper = new ObjectMapper();
    // To read date time field:
    mapper.registerModule(new JavaTimeModule());
    List<Version> actual = mapper.readValue(responseJson, new TypeReference<List<Version>>() {
    });
    assertThat(actual.size()).isEqualTo(2);
    assertThat(actual.get(0).getFileId()).isEqualTo(uuid);
    assertThat(actual.get(0).getContentsSha()).isEqualTo(sha1);
    assertThat(actual.get(1).getFileId()).isEqualTo(uuid);
    assertThat(actual.get(1).getContentsSha()).isEqualTo(sha2);
  }

  private Response getVersions(UUID filename) {
    return resource
        .client()
        .target("/rest/files/" + filename.toString() + "/versions")
        .request()
        .get();

  }

}
