package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.resources.HeaderLink.Rel;
import nl.knaw.huc.resources.HeaderLink.Uri;
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
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static nl.knaw.huc.resources.HeaderLink.Rel.ORIGINAL;
import static nl.knaw.huc.resources.HeaderLink.Rel.UP;
import static nl.knaw.huc.resources.HeaderLink.Rel.VERSION_HISTORY;
import static nl.knaw.huc.resources.HeaderLink.Uri.DOCUMENT;
import static nl.knaw.huc.resources.HeaderLink.Uri.DOCUMENT_METADATA;
import static nl.knaw.huc.resources.HeaderLink.Uri.FILE;
import static nl.knaw.huc.resources.HeaderLink.Uri.FILE_METADATA;
import static nl.knaw.huc.resources.HeaderLink.Uri.FILE_VERSIONS;

/**
 * The /find-task finds resources by external id and possible other parameters
 *
 * <p>Naming of task: stackoverflow.com/a/2141846
 */
@Path("/task/find/{externalId}")
@Api(tags = {"task", "find"})
public class FindResource {

  private static final Logger log = LoggerFactory.getLogger(FindResource.class);

  private final TaskBuilderFactory factory;

  public FindResource(TaskBuilderFactory factory) {
    this.factory = factory;
  }

  @GET
  @Path("/document/metadata")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Find metadata of document by external ID, " +
      "with header links to original and parent resource", response = Map.class)
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

    final var result = task.run();
    final var docId = result.getDocument().getId();

    return Response
        .ok(result.getMetadata(), APPLICATION_JSON)
        .link(DOCUMENT_METADATA.build(docId), ORIGINAL)
        .link(DOCUMENT.build(docId), UP)
        .build();
  }

  @GET
  @Path("/file/metadata")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Find metadata of file by external ID and file type, " +
      "with header links to original, parent and type resource", response = Map.class)
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

    final var result = task.run();
    final var file = result.getFile();
    final var fileId = file.getId();
    final var typeId = file.getTypeId();

    return Response
        .ok(result.getMetadata(), APPLICATION_JSON)
        .link(FILE_METADATA.build(fileId), ORIGINAL)
        .link(FILE.build(fileId), UP)
        .link(Uri.TYPE.build(typeId), Rel.TYPE)
        .build();
  }

  @GET
  @Path("/file/contents")
  @Produces({APPLICATION_JSON, APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Find contents of latest file version by external ID and file type, " +
      "with header links to version history, parent and type resource", response = Map.class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "OK"),
      @ApiResponse(code = 404, message = "Given type, document or supposed contents could not be found")})
  public Response getLatestFileVersionContent(
      @NotNull @PathParam("externalId") String documentId,
      @NotNull @QueryParam("type") String typeName
  ) {
    log.debug("Get latest version contents: documentId={}; type={}", documentId, typeName);
    final var task = factory
        .getContentsFinderBuilder()
        .forExternalId(documentId)
        .withType(typeName)
        .build();

    final var result = task.run();
    final var contents = result.getContents();
    log.debug("Got latest version contents: {}", contents);

    final var fileId = result.getFileId();
    final var typeId = result.getTypeId();
    final var payload = contents.decompressIfCompressedSizeLessThan(2 * 1024 * 1024);
    return Response
        .ok(payload, APPLICATION_OCTET_STREAM)
        .link(FILE_VERSIONS.build(fileId), VERSION_HISTORY)
        .link(FILE.build(fileId), UP)
        .link(Uri.TYPE.build(typeId), Rel.TYPE)
        .header("Content-Disposition", "attachment;")
        .build();
  }

}
