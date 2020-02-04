package nl.knaw.huc.resources.rest;

import io.swagger.annotations.Api;
import nl.knaw.huc.api.FormType;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.service.TypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(tags = {"types"})
@Path("/rest/types")
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
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public void addType(
      @NotNull @Valid FormType form
  ) {
    var type = new Type(form.getName(), form.getMimetype());
    logger.debug("addType: type={}", type);
    typeService.create(type);
  }
}