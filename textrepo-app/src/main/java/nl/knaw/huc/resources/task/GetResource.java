package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.resources.rest.DocumentMetadataResource;
import nl.knaw.huc.resources.rest.DocumentsResource;
import nl.knaw.huc.resources.rest.FileMetadataResource;
import nl.knaw.huc.resources.rest.FilesResource;
import nl.knaw.huc.resources.rest.TypesResource;
import nl.knaw.huc.service.task.TaskBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.UriBuilder.fromResource;
import static nl.knaw.huc.service.ContentsService.abbreviateMiddle;

@Path("/task/get/{externalId}")
@Api(tags = {"task", "get"})
public class GetResource {

  private static final Logger log = LoggerFactory.getLogger(GetResource.class);
  private final TaskBuilderFactory factory;

  public GetResource(TaskBuilderFactory factory) {
    this.factory = factory;
  }

  @GET
  @Path("/document/metadata")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get metadata of document by external ID, " +
      "with header links to original and parent resource", response = byte[].class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "OK"),
      @ApiResponse(code = 404, message = "Given document could not be found")})
  public Response getDocumentMetadata(
      @NotNull @PathParam("externalId") String externalId
  ) {
    log.debug("Find metadata of document: externalId={}", externalId);
    final var task = factory
        .getDocumentMetadataGetter()
        .forExternalId(externalId)
        .build();

    final var docMeta = task
        .run();

    var resourceLink = fromResource(DocumentMetadataResource.class).build(docMeta.getDocument().getId());
    var parentResourceLink = fromResource(DocumentsResource.class).path("/{id}").build(docMeta.getDocument().getId());

    return Response
        .ok(docMeta.getMetadata(), APPLICATION_JSON)
        .header("Link","<" + resourceLink + ">; rel=original")
        .header("Link","<" + parentResourceLink + ">; rel=up")
        .build();
  }

  @GET
  @Path("/file/metadata")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get metadata of file by external ID, " +
      "with header links to original, parent and type resource", response = byte[].class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "OK"),
      @ApiResponse(code = 404, message = "Given file could not be found")})
  public Response getFileMetadata(
      @NotNull @PathParam("externalId") String externalId,
      @NotNull @QueryParam("type") String typeName
  ) {
    log.debug("Find metadata of file: externalId={}, type={}", externalId, typeName);
    final var task = factory
        .getFileMetadataGetter()
        .forExternalId(externalId)
        .forType(typeName)
        .build();

    final var fileMetadata = task.run();

    var resourceLink = fromResource(FileMetadataResource.class).build(fileMetadata.getFile().getId());
    var parentResourceLink = fromResource(FilesResource.class).path("/{id}").build(fileMetadata.getFile().getId());
    var typeResourceLink = fromResource(TypesResource.class).path("/{id}").build(fileMetadata.getFile().getTypeId());

    return Response
        .ok(fileMetadata.getMetadata(), APPLICATION_JSON)
        .header("Link","<" + resourceLink + ">; rel=original")
        .header("Link","<" + parentResourceLink + ">; rel=up")
        .header("Link","<" + typeResourceLink + ">; rel=type")
        .build();
  }

  @GET
  @Path("/file/contents")
  @Produces({APPLICATION_JSON, APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Get contents of latest file version by external ID and file type", response = byte[].class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "OK"),
      @ApiResponse(code = 404, message = "Given type, document or supposed contents could not be found")})
  public Response getLatestFileVersionContent(
      @NotNull @PathParam("externalId") String documentId,
      @NotNull @QueryParam("type") String typeName
  ) {
    log.debug("Get latest version contents: documentId={}; type={}", documentId, typeName);
    final var task = factory.getContentsFinderBuilder()
                            .forExternalId(documentId)
                            .withType(typeName)
                            .build();
    final var bytes = task.run().getContents();

    log.debug("Got latest version contents: {}", abbreviateMiddle(bytes));

    return Response
        .ok(bytes, APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment;")
        .build();
  }

}
