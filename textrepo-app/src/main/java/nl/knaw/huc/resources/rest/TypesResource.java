package nl.knaw.huc.resources.rest;

import io.swagger.annotations.Api;
import nl.knaw.huc.api.FormType;
import nl.knaw.huc.api.ResultTextrepoFile;
import nl.knaw.huc.api.ResultType;
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
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(tags = {"types"})
@Path("/rest/types")
public class TypesResource {
  private static final Logger logger = LoggerFactory.getLogger(TypesResource.class);

  private final TypeService typeService;

  public TypesResource(TypeService typeService) {
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
  public Response addType(
      @NotNull @Valid FormType form
  ) {
    var type = new Type(form.getName(), form.getMimetype());
    logger.debug("addType: type={}", type);
    type.setId(typeService.create(type));
    return Response.ok(new ResultType(type)).build();
  }
}
