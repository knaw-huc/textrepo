package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.service.task.TaskBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/task/get")
@Api(tags = {"task", "get"})
public class GetResource {

  private static final Logger log = LoggerFactory.getLogger(GetResource.class);
  private final TaskBuilderFactory factory;

  public GetResource(TaskBuilderFactory factory) {
    this.factory = factory;
  }

  @GET
  @Path("{externalId}/document/metadata")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get metadata of document by external ID", response = byte[].class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "OK"),
      @ApiResponse(code = 404, message = "Given document could not be found")})
  public Response getDocumentMetadata(
      @NotNull @PathParam("externalId") String externalId
  ) {
    log.debug("Find metadata of document by external ID: externalId={}", externalId);
    final var task = factory
        .getDocumentMetadataGetter()
        .forExternalId(externalId)
        .build();

    final var metadata = task
        .run();

    return Response
        .ok(metadata, APPLICATION_JSON)
        .header("Content-Disposition", "attachment;")
        .build();
  }

}
