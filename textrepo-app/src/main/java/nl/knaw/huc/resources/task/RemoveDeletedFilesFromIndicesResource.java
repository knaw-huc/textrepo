package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huc.service.task.TaskBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(tags = {"task", "index", "delete"})
@Path("/task/delete/deleted-files")
public class RemoveDeletedFilesFromIndicesResource {

  private static final Logger log = LoggerFactory.getLogger(RemoveDeletedFilesFromIndicesResource.class);

  private final TaskBuilderFactory factory;

  public RemoveDeletedFilesFromIndicesResource(TaskBuilderFactory factory) {
    this.factory = requireNonNull(factory);
  }

  @DELETE
  @ApiOperation(value = "Delete all ES docs from all indices " +
      "that have an ID which does not exist in the files table .")
  @Path("documents/{externalId}")
  @Produces(APPLICATION_JSON)
  public Response removeDeletedFilesFromIndices() {
    log.debug("Delete all remove files");

    final var deletedFiles = factory
        .getRemoveDeletedFilesFromIndicesBuilder()
        .build()
        .run();

    log.debug(format("Removed all deleted files from all indices: %s", deletedFiles));
    return Response.ok().build();
  }
}
