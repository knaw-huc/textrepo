package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import nl.knaw.huc.service.task.TaskBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Api(tags = {"task", "index"})
@Path("task/index")
public class IndexResource {
  private static final Logger LOG = LoggerFactory.getLogger(IndexResource.class);

  private final TaskBuilderFactory factory;

  public IndexResource(TaskBuilderFactory factory) {
    this.factory = factory;
  }

  @POST
  @Path("/document/{externalId}/{type}")
  public Response indexDocument(@PathParam("externalId") String externalId, @PathParam("type") String type) {
    LOG.debug("Indexing document: {}, type: {}", externalId, type);
    final var task = factory.getDocumentIndexBuilder()
                            .forExternalId(externalId)
                            .withType(type)
                            .build();
    task.run();
    return Response.accepted().build();
  }
}
