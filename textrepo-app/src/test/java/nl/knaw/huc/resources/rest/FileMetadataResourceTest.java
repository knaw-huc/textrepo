package nl.knaw.huc.resources.rest;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.db.FileMetadataDao;
import nl.knaw.huc.service.file.metadata.FileMetadataService;
import nl.knaw.huc.service.file.metadata.JdbiFileMetadataService;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class FileMetadataResourceTest {

  private static final UUID fileId = UUID.fromString("adefaced-cafe-babe-0001-added1234567");

  private static final Jdbi jdbi = mock(Jdbi.class);

  private static final FileMetadataService FILE_METADATA_SERVICE = new JdbiFileMetadataService(jdbi);

  private static final FileMetadataDao FILE_METADATA_DAO = mock(FileMetadataDao.class);

  public static final ResourceExtension resource = ResourceExtension
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new FileMetadataResource(FILE_METADATA_SERVICE))
      .build();

  @Captor
  private ArgumentCaptor<MetadataEntry> metadataCaptor;
  @Captor
  private ArgumentCaptor<UUID> fileIdCaptor;

  @BeforeEach
  public void setupMocks() {
    when(jdbi.onDemand(any())).thenReturn(FILE_METADATA_DAO);
    MockitoAnnotations.initMocks(this);
  }

  @AfterEach
  public void resetMocks() {
    reset(jdbi, FILE_METADATA_DAO);
  }

  @Test
  public void testPutMetadata_putsMetadata() throws IOException {
    var key = "gene";
    var value = "FOXP2";
    var metadata = new MetadataEntry(key, value);

    var response = putMetadata(fileId, metadata);

    assertThat(response.getStatus()).isEqualTo(200);
    verify(FILE_METADATA_DAO, times(1)).upsert(fileIdCaptor.capture(), metadataCaptor.capture());
    assertThat(fileIdCaptor.getValue()).isEqualTo(fileId);
    assertThat(metadataCaptor.getValue().getKey()).isEqualTo(key);
    assertThat(metadataCaptor.getValue().getValue()).isEqualTo(value);
  }

  private Response putMetadata(UUID fileId, MetadataEntry metadataEntry) {
    return resource
        .client()
        .target("/rest/files/" + fileId.toString() + "/metadata/" + metadataEntry.getKey())
        .request()
        .put(Entity.json(metadataEntry.getValue()));

  }


}
