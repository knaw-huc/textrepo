package nl.knaw.huc.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.db.VersionDao;
import nl.knaw.huc.service.ContentsService;
import nl.knaw.huc.service.JdbiVersionService;
import nl.knaw.huc.service.VersionService;
import nl.knaw.huc.service.index.ElasticCustomFacetIndexer;
import nl.knaw.huc.service.index.ElasticDocumentIndexer;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VersionsResourceTest {
  private static final UUID uuid = UUID.fromString("0defaced-cafe-babe-dada-deadbeefc2c6");

  private static final Jdbi jdbi = mock(Jdbi.class);

  private static final VersionService versionService = new JdbiVersionService(
      jdbi,
      mock(ContentsService.class),
      mock(ElasticDocumentIndexer.class),
      newArrayList(mock(ElasticCustomFacetIndexer.class))
  );

  private static final VersionDao versionDao = mock(VersionDao.class);

  @ClassRule
  public static final ResourceTestRule resource = ResourceTestRule
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new VersionsResource(versionService))
      .build();

  @Before
  public void setupMocks() {
    when(jdbi.onDemand(any())).thenReturn(versionDao);
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void resetMocks() {
    reset(jdbi, versionDao);
  }

  @Test
  public void testGetVersions_returnsVersions() throws IOException {
    List<Version> versions = new ArrayList<>();
    var sha1 = "fcd01d3b5648843931feb9ef4468250ac1a968a41add37f663af3bb0";
    var version1 = new Version(uuid, LocalDateTime.now(), sha1);
    versions.add(version1);
    var sha2 = "d476f2f6e00deaa918dfbec79545412134e02095f870269175b89376";
    var version2 = new Version(uuid, LocalDateTime.now(), sha2);
    versions.add(version2);
    when(versionDao.findByUuid(any())).thenReturn(versions);

    var response = getVersions(uuid);

    verify(versionDao, times(1)).findByUuid(uuid);
    var responseJson = response.readEntity(String.class);
    var mapper = new ObjectMapper();
    // To read date time field:
    mapper.registerModule(new JavaTimeModule());
    List<Version> actual = mapper.readValue(responseJson, new TypeReference<List<Version>>(){});
    assertThat(actual.size()).isEqualTo(2);
    assertThat(actual.get(0).getDocumentUuid()).isEqualTo(uuid);
    assertThat(actual.get(0).getContentsSha()).isEqualTo(sha1);
    assertThat(actual.get(1).getDocumentUuid()).isEqualTo(uuid);
    assertThat(actual.get(1).getContentsSha()).isEqualTo(sha2);
  }

  private Response getVersions(UUID filename) {
    return resource
        .client()
        .target("/documents/" + filename.toString() + "/versions")
        .request()
        .get();

  }

}
