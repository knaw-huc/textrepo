package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huc.service.task.TaskBuilderFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

@Path("/task/latest")
@Api(tags = {"task", "latest"})
public class FindResource {
  private final TaskBuilderFactory factory;

  public FindResource(TaskBuilderFactory factory) {
    this.factory = factory;
  }

  @GET
  @Produces(APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Get latest contents of typed file for a document")
  public Response findLatestVersion(@QueryParam("externalId") String documentId,
                                    @QueryParam("typeName") String typeName) {
    final var task = factory.getContentsFinderBuilder()
                            .forExternalId(documentId)
                            .withType(typeName)
                            .build();
    final var contents = task.run();
    return Response
        .ok(contents.getContents(), APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment;")
        .build();
  }
}
