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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static nl.knaw.huc.service.ContentsService.abbreviateMiddle;

@Path("/task/latest")
@Api(tags = {"task", "latest"})
public class FindResource {

  private static final Logger log = LoggerFactory.getLogger(FindResource.class);
  private final TaskBuilderFactory factory;

  public FindResource(TaskBuilderFactory factory) {
    this.factory = factory;
  }

  @GET
  @Produces({APPLICATION_JSON, APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Get latest contents of typed file for a document", response = byte[].class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "OK"),
      @ApiResponse(code = 404, message = "Given type, document or supposed contents could not be found")})
  public Response findLatestVersion(
      @NotNull @QueryParam("externalId") String documentId,
      @NotNull @QueryParam("typeName") String typeName
  ) {
    log.debug("Find latest version contents: documentId={}; typeName={}", documentId, typeName);
    final var task = factory.getContentsFinderBuilder()
                            .forExternalId(documentId)
                            .withType(typeName)
                            .build();
    final var bytes = task.run().getContents();
    log.debug("Find latest version contents: {}", abbreviateMiddle(bytes));
    return Response
        .ok(bytes, APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment;")
        .build();
  }
}
