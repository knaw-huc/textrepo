package nl.knaw.huc.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.service.DocumentService;
import nl.knaw.huc.service.ElasticFileIndexService;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DocumentsResourceTest {
  private static final DocumentService documentService = mock(DocumentService.class);
  private static final String uuid = "b59c2b24-cafe-babe-9bb3-deadbeefc2c6";
  private static final String content = "hello test";
  private final static String sha224 = "55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6";
  private static final ElasticFileIndexService fileIndexService = mock(ElasticFileIndexService.class);

  @ClassRule
  public static final ResourceTestRule resource = ResourceTestRule
          .builder()
          .addProvider(MultiPartFeature.class)
          .addResource(new DocumentsResource(documentService))
          .build();

  @Test
  public void testPostDocument_returns201CreatedWithLocationHeader_whenFileUploaded() {
    Version mockVersion = mock(Version.class);
    when(mockVersion.getDocumentUuid()).thenReturn(UUID.fromString(uuid));
    when(documentService.addDocument(any())).thenReturn(mockVersion);

    var multiPart = new FormDataMultiPart()
            .field("file", content);

    final var request = resource
            .client()
            .register(MultiPartFeature.class)
            .target("/documents")
            .request();

    final var entity = Entity.entity(multiPart, multiPart.getMediaType());
    var response = request.post(entity);

    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeaderString("Location")).endsWith("documents/" + uuid);
  }

}
