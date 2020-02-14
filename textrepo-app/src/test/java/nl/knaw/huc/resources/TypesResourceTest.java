package nl.knaw.huc.resources;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.api.FormType;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.resources.rest.TypesResource;
import nl.knaw.huc.service.JdbiTypeService;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import static javax.ws.rs.client.Entity.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
public class TypesResourceTest {

  @Captor
  private ArgumentCaptor<Type> typeCaptor;

  private static final JdbiTypeService typeService = mock(JdbiTypeService.class);

  public static final ResourceExtension resource = ResourceExtension
      .builder()
      .addProvider(MultiPartFeature.class)
      .addResource(new TypesResource(typeService))
      .build();

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @AfterEach
  public void resetMocks() {
    reset(typeService);
  }

  @Test
  public void testAddType_createsType() {
    var expectedName = "test-type";
    var expectedMimetype = "application/xml";
    var form = new FormType(expectedName, expectedMimetype);
    var type = new Type(expectedName, expectedMimetype);
    type.setId((short) 456);
    when(typeService.create(any())).thenReturn(type);

    var response = resource
        .client()
        .target("/rest/types")
        .request()
        .post(json(form));

    assertThat(response.getStatus()).isEqualTo(200);

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
        .target("/rest/types")
        .request()
        .post(json(type));

    assertThat(response.getStatus()).isEqualTo(422);
    var responseBody = response.readEntity(String.class);
    assertThat(responseBody).contains("name is mandatory");
    assertThat(responseBody).contains("mimetype is mandatory");

    verify(typeService, never()).create(any());
  }

}
