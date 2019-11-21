package nl.knaw.huc.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.db.MetadataDao;
import nl.knaw.huc.service.JdbiMetadataService;
import nl.knaw.huc.service.MetadataService;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetadataResourceTest {
  private static final UUID fileId = UUID.fromString("adefaced-cafe-babe-0001-added1234567");

  private static final Jdbi jdbi = mock(Jdbi.class);

  private static final MetadataService metadataService = new JdbiMetadataService(jdbi);

  private static final MetadataDao metadataDao = mock(MetadataDao.class);

  @ClassRule
  public static final ResourceTestRule resource = ResourceTestRule
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new MetadataResource(metadataService))
      .build();

  @Captor
  private ArgumentCaptor<MetadataEntry> metadataCaptor;
  @Captor
  private ArgumentCaptor<UUID> fileIdCaptor;

  @Before
  public void setupMocks() {
    when(jdbi.onDemand(any())).thenReturn(metadataDao);
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void resetMocks() {
    reset(jdbi, metadataDao);
  }

  @Test
  public void testGetVersions_returnsVersions() throws IOException {
    var key = "gene";
    var value = "FOXP2";
    var metadata = new MetadataEntry(key, value);

    var response = putMetadata(fileId, metadata);

    assertThat(response.getStatus()).isEqualTo(200);
    verify(metadataDao, times(1)).update(fileIdCaptor.capture(), metadataCaptor.capture());
    assertThat(fileIdCaptor.getValue()).isEqualTo(fileId);
    assertThat(metadataCaptor.getValue().getKey()).isEqualTo(key);
    assertThat(metadataCaptor.getValue().getValue()).isEqualTo(value);
  }

  private Response putMetadata(UUID fileId, MetadataEntry metadataEntry) {
    return resource
        .client()
        .target("/files/" + fileId.toString() + "/metadata/" + metadataEntry.getKey())
        .request()
        .put(Entity.json(metadataEntry.getValue()));

  }


}
