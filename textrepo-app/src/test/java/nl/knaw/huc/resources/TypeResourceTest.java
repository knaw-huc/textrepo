package nl.knaw.huc.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huc.api.FormType;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.service.JdbiTypeService;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.Entity;

import static javax.ws.rs.client.Entity.json;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class TypeResourceTest {

  @Captor
  private ArgumentCaptor<Type> typeCaptor;

  private static final JdbiTypeService typeService = mock(JdbiTypeService.class);

  @ClassRule
  public static final ResourceTestRule resource = ResourceTestRule
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new TypeResource(typeService))
      .build();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void resetMocks() {
    reset(typeService);
  }

  @Test
  public void testAddType_createsType() {
    var expectedName = "test-type";
    var expectedMimetype = "application/xml";
    var type = new FormType(expectedName, expectedMimetype);
    var response = resource
        .client()
        .target("/types")
        .request()
        .post(json(type));

    assertThat(response.getStatus()).isEqualTo(204);

    verify(typeService, times(1)).create(typeCaptor.capture());
    var toCreate = typeCaptor.getValue();
    assertThat(toCreate.getName()).isEqualTo(expectedName);
    assertThat(toCreate.getMimetype()).isEqualTo(expectedMimetype);
  }

  @Test
  public void testAddType_verifiesInput() {
    var type = new FormType("", "");

    var response = resource
        .client()
        .target("/types")
        .request()
        .post(json(type));

    assertThat(response.getStatus()).isEqualTo(422);
    var responseBody = response.readEntity(String.class);
    assertThat(responseBody).contains("name is mandatory");
    assertThat(responseBody).contains("mimetype is mandatory");

    verify(typeService, never()).create(any());
  }

}
