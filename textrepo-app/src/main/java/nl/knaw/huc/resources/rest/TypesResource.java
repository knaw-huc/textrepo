package nl.knaw.huc.resources.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huc.api.FormType;
import nl.knaw.huc.api.ResultType;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.service.type.TypeService;
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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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
  public Response createType(
      @Valid @NotNull FormType form
  ) {
    var type = new Type(form.getName(), form.getMimetype());
    log.debug("Create type: type={}", type);
    var created = typeService.create(type);
    log.debug("Created type: {}", created);
    return Response.ok(new ResultType(created)).build();
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve types")
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
  @ApiOperation(value = "Retrieve types")
  public Response getType(
      @NotNull @PathParam("id") Short id
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
  public Response putType(
      @NotNull @PathParam("id") Short id,
      @NotNull @Valid FormType form
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
  public Response deleteType(
      @NotNull @PathParam("id") Short id
  ) {
    log.debug("Delete type: id={}", id);
    typeService.delete(id);
    log.debug("Deleted type");
    return Response.ok().build();
  }

}
