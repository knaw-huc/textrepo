package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.resources.rest.DocumentMetadataResource;
import nl.knaw.huc.resources.rest.DocumentsResource;
import nl.knaw.huc.resources.rest.FileMetadataResource;
import nl.knaw.huc.resources.rest.FileVersionsResource;
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
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.UriBuilder.fromResource;

/**
 * The /find-task finds resources by external id and possible other parameters
 *
 * <p>Naming of task: stackoverflow.com/a/2141846
 */
@Path("/task/find/{externalId}")
@Api(tags = {"task", "find"})
public class FindResource {

  public static final String REL_ORIGINAL = "original";
  public static final String REL_TYPE = Link.TYPE;
  public static final String REL_UP = "up";
  public static final String REL_VERSION_HISTORY = "version-history";

  private static final UriBuilder DOCUMENTS = fromResource(DocumentsResource.class).path("/{id}");
  private static final UriBuilder DOCUMENT_METADATA = fromResource(DocumentMetadataResource.class);
  private static final UriBuilder FILES = fromResource(FilesResource.class).path("/{id}");
  private static final UriBuilder FILE_METADATA = fromResource(FileMetadataResource.class);
  private static final UriBuilder FILE_VERSIONS = fromResource(FileVersionsResource.class);
  private static final UriBuilder TYPES = fromResource(TypesResource.class).path("/{id}");

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
        .link(DOCUMENT_METADATA.build(docId), REL_ORIGINAL)
        .link(DOCUMENTS.build(docId), REL_UP)
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
        .link(FILE_METADATA.build(fileId), REL_ORIGINAL)
        .link(FILES.build(fileId), REL_UP)
        .link(TYPES.build(typeId), REL_TYPE)
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
    final byte[] payload = contents.decompressIfCompressedSizeLessThan(2 * 1024 * 1024);
    return Response
        .ok(payload, APPLICATION_OCTET_STREAM)
        .link(FILE_VERSIONS.build(fileId), REL_VERSION_HISTORY)
        .link(FILES.build(fileId), REL_UP)
        .link(TYPES.build(typeId), REL_TYPE)
        .header("Content-Disposition", "attachment;")
        .build();
  }

}
