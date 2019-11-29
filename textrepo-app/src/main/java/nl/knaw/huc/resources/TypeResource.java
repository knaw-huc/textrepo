package nl.knaw.huc.resources;

import io.swagger.annotations.Api;
import nl.knaw.huc.service.TypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(tags = {"types"})
@Path("/types")
public class TypeResource {
  private static final Logger logger = LoggerFactory.getLogger(TypeResource.class);

  private final TypeService typeService;

  public TypeResource(TypeService typeService) {
    this.typeService = typeService;
  }

  @GET
  @Produces(APPLICATION_JSON)
  public List<String> getTypes() {
    return typeService.list();
  }

  @POST
  public void addType(String name) {
    logger.debug("addType: name=[{}]", name);
    typeService.create(name);
  }
}
