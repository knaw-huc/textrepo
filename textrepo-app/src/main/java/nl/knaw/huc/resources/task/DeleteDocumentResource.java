package nl.knaw.huc.resources.task;

import nl.knaw.huc.service.task.TaskBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/task/delete")
public class DeleteDocumentResource {
  private static final Logger LOG = LoggerFactory.getLogger(DeleteDocumentResource.class);

  private final TaskBuilderFactory factory;

  public DeleteDocumentResource(TaskBuilderFactory factory) {
    this.factory = factory;
  }

  @DELETE
  @Path("documents/{externalId}")
  public Response deleteDocument(
      @NotBlank @PathParam("externalId") String externalId
  ) {
    LOG.debug("delete document: externalId={}", externalId);

    final var task = factory.getDocumentDeleteBuilder().forExternalId(externalId).build();
    final var doc = task.run();

    LOG.debug("deleted document");
    return Response.ok(doc).build();
  }
}
