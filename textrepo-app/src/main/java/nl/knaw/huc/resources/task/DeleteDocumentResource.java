package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huc.service.task.TaskBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(tags = {"task", "documents"})
@Path("/task/delete")
public class DeleteDocumentResource {

  private static final Logger log = LoggerFactory.getLogger(DeleteDocumentResource.class);

  private final TaskBuilderFactory factory;

  public DeleteDocumentResource(TaskBuilderFactory factory) {
    this.factory = requireNonNull(factory);
  }

  @DELETE
  @ApiOperation(value = "Delete a document including its metadata, files, versions and contents. " +
      "Contents are only deleted when not referenced by any other versions.")
  @Path("documents/{externalId}")
  @Produces(APPLICATION_JSON)
  public Response deleteDocumentRecursively(
      @NotBlank @PathParam("externalId") String externalId
  ) {
    log.debug("Delete document: externalId={}", externalId);

    final var task = factory.getDocumentDeleteBuilder().forExternalId(externalId).build();
    final var doc = task.run();

    log.debug("Deleted document");
    return Response.ok(doc).build();
  }
}
