package nl.knaw.huc.resources.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huc.api.FormType;
import nl.knaw.huc.api.ResultType;
import nl.knaw.huc.core.Type;
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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(tags = {"types"})
@Path("/rest/types")
public class TypesResource {
  private static final Logger logger = LoggerFactory.getLogger(TypesResource.class);

  private final TypeService typeService;

  public TypesResource(TypeService typeService) {
    this.typeService = typeService;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create type")
  public Response post(
      @NotNull @Valid FormType form
  ) {
    var type = new Type(form.getName(), form.getMimetype());
    logger.debug("Create type: type={}", type);
    var created = typeService.create(type);
    return Response.ok(new ResultType(created)).build();
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve types")
  public Response getAll() {
    logger.debug("Retrieve types");
    var all = typeService
        .list()
        .stream()
        .map(ResultType::new)
        .collect(toList());
    return Response.ok(all).build();
  }

  @GET
  @Path("/{id}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve types")
  public Response get(
      @NotNull @PathParam("id") Short id
  ) {
    logger.debug("Retrieve type: id={}", id);
    var type = typeService.getType(id);
    return Response.ok(new ResultType(type)).build();
  }

  @PUT
  @Path("/{id}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Update type")
  public Response put(
      @NotNull @PathParam("id") Short id,
      @NotNull @Valid FormType form
  ) {
    var type = new Type(form.getName(), form.getMimetype());
    type.setId(id);
    logger.debug("Put type: type={}", type);
    typeService.upsert(type);
    return Response.ok(new ResultType(type)).build();
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(value = "Delete type")
  public Response delete(
      @NotNull @PathParam("id") Short id
  ) {
    logger.debug("Delete type: id={}", id);
    typeService.delete(id);
    return Response.ok().build();
  }

}
