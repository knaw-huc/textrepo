package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nl.knaw.huc.service.task.TaskBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import static java.util.Objects.requireNonNull;

@Api(tags = {"task", "index"})
@Path("task/index")
public class IndexResource {

  private static final Logger log = LoggerFactory.getLogger(IndexResource.class);

  private final TaskBuilderFactory factory;

  public IndexResource(TaskBuilderFactory factory) {
    this.factory = requireNonNull(factory);
  }

  @POST
  @Path("/file/{externalId}/{type}")
  @ApiOperation("Index a file of document by externalId and file type. " +
      "When a file has no versions, an empty string is used for contents.")
  public Response indexDocument(
      @PathParam("externalId")
      @ApiParam(example = "document_1234", required = true)
      @Valid
          String externalId,
      @PathParam("type")
      @ApiParam(example = "plaintext", required = true)
      @NotBlank
          String type
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
  @Path("/type/{type}")
  @ApiOperation("Index all files of type. Includes files without versions")
  public Response indexAll(
      @PathParam("type")
      @ApiParam(example = "plaintext", required = true)
      @NotBlank
          String type
  ) {
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
  @Path("/indexer/{name}")
  @ApiOperation("Index single index by its indexer name. Includes files without versions")
  public Response indexSingleIndex(
      @PathParam("name")
      @ApiParam(example = "file", required = true)
      @NotBlank
          String name
  ) {
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
