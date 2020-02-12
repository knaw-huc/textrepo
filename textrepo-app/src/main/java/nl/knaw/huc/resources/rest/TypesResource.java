package nl.knaw.huc.resources.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.FormType;
import nl.knaw.huc.api.ResultType;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.exceptions.MethodNotAllowedException;
import nl.knaw.huc.service.TypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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

  private static final String PUT_ERROR_MSG = "Putting types not supported at the moment";
  private static final String DELETE_ERROR_MSG = "Deleting types not supported at the moment";

  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create type")
  public Response post(
      @NotNull @Valid FormType form
  ) {
    var type = new Type(form.getName(), form.getMimetype());
    logger.debug("Create type: type={}", type);
    type.setId(typeService.create(type));
    return Response.ok(new ResultType(type)).build();
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve types")
  public List<String> get() {
    logger.debug("Retrieve types");
    return typeService.list();
  }

  @PUT
  @Path("/{id}")
  @ApiOperation(value = PUT_ERROR_MSG)
  @ApiResponses(value = {@ApiResponse(code = 405, message = PUT_ERROR_MSG)})
  public Response put() {
    throw new MethodNotAllowedException(PUT_ERROR_MSG);
  }

  @DELETE
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = DELETE_ERROR_MSG)
  @ApiResponses(value = {@ApiResponse(code = 405, message = DELETE_ERROR_MSG)})
  public Response delete() {
    throw new MethodNotAllowedException(DELETE_ERROR_MSG);
  }

}
