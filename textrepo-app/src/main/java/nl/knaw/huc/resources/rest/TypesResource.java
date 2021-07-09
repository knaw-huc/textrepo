package nl.knaw.huc.resources.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.FormType;
import nl.knaw.huc.api.ResultType;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.service.type.TypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huc.resources.HeaderLink.Uri.TYPE;

@Api(tags = {"types"})
@Path("/rest/types")
public class TypesResource {

  private static final Logger log = LoggerFactory.getLogger(TypesResource.class);

  private final TypeService typeService;

  public TypesResource(TypeService typeService) {
    this.typeService = requireNonNull(typeService);
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create type")
  @ApiResponses(value = {@ApiResponse(code = 201, response = ResultType.class, message = "Created")})
  public Response createType(
      @Valid
      @NotNull
          FormType form
  ) {
    var type = new Type(form.getName(), form.getMimetype());
    log.debug("Create type: type={}", type);
    var created = typeService.create(type);
    log.debug("Created type: {}", created);
    return Response
        .created(TYPE.build(created.getId()))
        .entity(new ResultType(created))
        .build();
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve types")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultType.class, responseContainer = "List",
      message = "OK")})
  public Response getTypes() {
    log.debug("Retrieve all types");
    var all = typeService
        .list()
        .stream()
        .map(ResultType::new)
        .collect(toList());
    log.debug("Retrieved all types: {}", all);
    return Response.ok(all).build();
  }

  @GET
  @Path("/{id}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve type")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultType.class, message = "OK")})
  public Response getType(
      @PathParam("id")
      @ApiParam(required = true, example = "1")
      @NotNull
          Short id
  ) {
    log.debug("Retrieve type: id={}", id);
    var type = typeService.getType(id);
    log.debug("Retrieved type: {}", type);
    return Response.ok(new ResultType(type)).build();
  }

  @PUT
  @Path("/{id}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create or update type")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultType.class, message = "OK")})
  public Response putType(
      @PathParam("id")
      @NotNull
          Short id,
      @Valid
      @NotNull
          FormType form
  ) {
    log.debug("Create or update type: id={}; type={}", id, form);
    var type = new Type(form.getName(), form.getMimetype());
    type.setId(id);
    typeService.upsert(type);
    log.debug("Created or updated type: {}", type);
    return Response.ok(new ResultType(type)).build();
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(value = "Delete type")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response deleteType(
      @PathParam("id")
      @NotNull
          Short id
  ) {
    log.debug("Delete type: id={}", id);
    typeService.delete(id);
    log.debug("Deleted type");
    return Response.ok().build();
  }

}
