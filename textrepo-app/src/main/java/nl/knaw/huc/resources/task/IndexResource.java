package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

  private static final Logger log = LoggerFactory.getLogger(IndexResource.class);

  private final TaskBuilderFactory factory;

  public IndexResource(TaskBuilderFactory factory) {
    this.factory = factory;
  }

  @POST
  @Path("/document/{externalId}/{type}")
  @ApiOperation("Index file of document by externalId and file type")
  public Response indexDocument(
      @PathParam("externalId") String externalId,
      @PathParam("type") String type
  ) {
    log.debug("Index document: externalId={}; type={}", externalId, type);
    final var task = factory
        .getIndexBuilder()
        .forExternalId(externalId)
        .withType(type)
        .build();
    task.run();
    log.debug("Indexed document");
    return Response.accepted().build();
  }

  @POST
  @Path("/files/{type}")
  @ApiOperation("Index all files by type")
  public Response indexAll(@PathParam("type") String type) {
    log.debug("Index all files of type: type={}", type);
    final var task = factory
        .getIndexBuilder()
        .withType(type)
        .build();
    var result = task.run();
    log.debug(result);
    return Response.ok().build();
  }

  @POST
  @Path("/{name}")
  @ApiOperation("Index all files of a single index")
  public Response indexSingleIndex(@PathParam("name") String name) {
    log.debug("Index all files of index: index={}", name);
    final var task = factory
        .getIndexBuilder()
        .forIndex(name)
        .build();
    task.run();
    log.debug("Indexed all files of index");
    return Response.ok().build();
  }

}
