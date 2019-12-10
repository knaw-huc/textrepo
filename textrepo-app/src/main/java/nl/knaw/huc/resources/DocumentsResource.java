package nl.knaw.huc.resources;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.service.FileService;
import nl.knaw.huc.service.JdbiDocumentService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.resources.ResourceUtils.readContent;

@Path("/documents")
public class DocumentsResource {
  private static final Logger LOG = LoggerFactory.getLogger(DocumentsResource.class);

  private final JdbiDocumentService documentService;
  private final FileService fileService;

  public DocumentsResource(JdbiDocumentService documentService, FileService fileService) {
    this.documentService = documentService;
    this.fileService = fileService;
  }

  @POST
  // E.g., POST <new contents> to /documents/ed311646-1b2d-11ea-a290-53729ef3c030?type=FoLiA
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create a new document by uploading contents for one of its file types")
  @ApiResponses(value = {@ApiResponse(code = 201, message = "CREATED")})
  public Response addDocument(
      @QueryParam("type") @Nonnull String type,
      @FormDataParam("contents") InputStream uploadedInputStream,
      @FormDataParam("contents") FormDataContentDisposition fileDetail,
      @FormDataParam("contents") FormDataBodyPart bodyPart
  ) {
    final var newFileId = fileService.createFile(type);
    LOG.debug("New file created with fileId={}", newFileId);

    fileService.createVersionWithFilenameMetadata(
        newFileId,
        readContent(uploadedInputStream),
        fileDetail.getFileName());
    LOG.debug("New version of {} created for content", newFileId);

    final var docId = documentService.createDocument(newFileId);

    return Response.created(locationOf(docId)).build();
  }

  @GET
  @Path("/{uuid}/{type}") // for now '/latest' is implicit
  // E.g., GET /documents/ed311646-1b2d-11ea-a290-53729ef3c030/FoLiA
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get latest version of document's file by type")
  @ApiResponses(value = {@ApiResponse(code = 200, response = Version.class, message = "OK")})
  public Response getLatestVersionByType(@PathParam("uuid") @Valid UUID docId,
                                         @PathParam("type") @Nonnull String typeName) {
    LOG.debug("getLatestVersionByType: docId={}, typeName={}", docId, typeName);
    final var fileId = documentService.findFileForType(docId, typeName);

    LOG.debug(" -> getting latest version of file: {}", fileId);
    var version = fileService.getLatestVersion(fileId);

    return Response.ok(version).build();
  }

  @PUT
  @Path("/{uuid}/{type}")
  // E.g., PUT <new contents> to /documents/ed311646-1b2d-11ea-a290-53729ef3c030/FoLiA
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  public Response updateDocumentByType(
      @PathParam("uuid") @Valid UUID docId,
      @PathParam("type") @Nonnull String type,
      @FormDataParam("contents") InputStream uploadedInputStream,
      @FormDataParam("contents") FormDataContentDisposition fileDetail,
      @FormDataParam("contents") FormDataBodyPart bodyPart
  ) {
    LOG.debug("Updating {} contents of document: {}", type, docId);

    final var fileId = documentService.findFileForType(docId, type);
    LOG.debug(" -> updating file: {}", fileId);

    final var version = fileService.createVersionWithFilenameMetadata(
        fileId,
        readContent(uploadedInputStream),
        fileDetail.getFileName());
    LOG.debug(" -> new version created: {}", version);

    return Response.ok().build();
  }

  private URI locationOf(UUID docId) {
    return UriBuilder.fromResource(DocumentsResource.class).path("{uuid}").build(docId);
  }

}
