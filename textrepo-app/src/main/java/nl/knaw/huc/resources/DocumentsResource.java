package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.service.DocumentService;
import nl.knaw.huc.service.FileService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.resources.ResourceUtils.readContent;

@Api(tags = {"documents"})
@Path("/documents")
public class DocumentsResource {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final DocumentService documentService;
  private final FileService fileService;

  public DocumentsResource(DocumentService documentService, FileService fileService) {
    this.documentService = documentService;
    this.fileService = fileService;
  }

  @POST
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create a new document by uploading contents for one of its file types")
  @ApiResponses(value = {@ApiResponse(code = 201, message = "CREATED")})
  public Response addDocument(
      @QueryParam("type") @NotBlank String type,
      @QueryParam("byFile") @NotNull @DefaultValue("false") Boolean byFile,
      @FormDataParam("contents") InputStream uploadedInputStream,
      @FormDataParam("contents") FormDataContentDisposition fileDetail
  ) {
    final var filename = fileDetail.getFileName();
    final var newFile = fileService.createFile(type);
    logger.debug("New file created with fileId={}", newFile);

    fileService.createVersionWithFilenameMetadata(
        newFile,
        readContent(uploadedInputStream),
        filename);
    logger.debug("New version of {} created for content", newFile);

    final Optional<UUID> existingDocId = byFile ? documentService.findDocumentByFilename(filename) : Optional.empty();
    logger.debug("Type: {}, filename: {}, existingDocId: {}", type, filename, existingDocId);

    final var docId = existingDocId.orElseGet(() -> documentService.createDocument(newFile.getId()));

    return Response.created(locationOf(docId)).build();
  }

  @GET
  @Path("/{uuid}/{type}") // for now '/latest' is implicit
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get latest version of document's file by type")
  @ApiResponses(value = {@ApiResponse(code = 200, response = Version.class, message = "OK")})
  public Response getLatestVersionByType(
      @PathParam("uuid") @Valid UUID docId,
      @NotNull @PathParam("type") String typeName
  ) {
    logger.debug("getLatestVersionByType: docId={}, typeName={}", docId, typeName);
    final var file = documentService.findFileForType(docId, typeName);

    logger.debug(" -> getting latest version of file: {}", file);
    var version = fileService.getLatestVersion(file.getId());

    return Response.ok(version).build();
  }

  @PUT
  @Path("/{uuid}/{type}")
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  public Response updateDocumentByType(
      @PathParam("uuid") @NotNull @Valid UUID docId,
      @PathParam("type") @NotBlank String type,
      @FormDataParam("contents") InputStream uploadedInputStream,
      @FormDataParam("contents") FormDataContentDisposition fileDetail,
      @FormDataParam("contents") FormDataBodyPart bodyPart
  ) {
    logger.debug("Updating {} contents of document: {}", type, docId);

    final var file = documentService.findFileForType(docId, type);
    logger.debug(" -> updating file: {}", file);

    final var version = fileService.createVersionWithFilenameMetadata(
        file,
        readContent(uploadedInputStream),
        fileDetail.getFileName()
    );
    logger.debug("Document [{}] has new version: {}", docId, version);
    return Response.ok().build();
  }

  @GET
  @Path("/{docId}/metadata")
  @Produces(APPLICATION_JSON)
  public Map<String, String> getDocumentMetadata(
      @PathParam("docId") @NotNull @Valid UUID docId
  ) {
    return documentService.getMetadata(docId);
  }

  @PUT
  @Path("/{docId}/metadata/{key}")
  @Timed
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Update value of a document metadata entry")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response updateMetadataEntry(
      @PathParam("docId") @NotNull @Valid UUID docId,
      @PathParam("key") @NotBlank String key,
      String value
  ) {
    logger.debug("updateMetadata: docId={}, key={}, value={}", docId, key, value);
    if (!documentService.updateMetadata(docId, new MetadataEntry(key, value))) {
      throw new NotFoundException(format("No metadata field [%s] found for document [%s]", key, docId));
    }
    return Response.ok().build();
  }

  private URI locationOf(UUID docId) {
    return UriBuilder.fromResource(DocumentsResource.class).path("{uuid}").build(docId);
  }

}
