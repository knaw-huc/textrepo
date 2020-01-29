package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.ResultDocument;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.service.DocumentService;
import nl.knaw.huc.service.FileService;
import nl.knaw.huc.service.MetadataService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
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
  private final MetadataService metadataService;

  public DocumentsResource(
      DocumentService documentService,
      FileService fileService,

      MetadataService metadataService
  ) {
    this.documentService = documentService;
    this.fileService = fileService;
    this.metadataService = metadataService;
  }

  @GET
  @Path("/{id}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get document by id")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultDocument.class, message = "OK")})
  public Response getDocument(
      @PathParam("id") @Valid UUID id
  ) {
    logger.debug("getDocument: id={}", id);
    final var doc = documentService.get(id);
    return Response.ok(new ResultDocument(doc)).build();
  }

  @PUT
  @Path("/{uuid}/{type}")
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  public Response updateDocumentByType(
      @PathParam("uuid") @NotNull @Valid UUID docId,
      @PathParam("type") @NotBlank String type,
      @FormDataParam("contents") InputStream uploadedInputStream,
      @FormDataParam("contents") FormDataContentDisposition fileDetail
  ) {
    logger.debug("Updating {} contents of document: {}", type, docId);
    return updateFileByTypeAndDocId(type, docId, uploadedInputStream, fileDetail);
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

  private URI locationOf(UUID docId, String type) {
    return UriBuilder.fromResource(DocumentsResource.class).path("{uuid}/{type}").build(docId, type);
  }

  /**
   * TODO: move to service layer
   */
  private Response updateFileByTypeAndDocId(
      String type,
      UUID docId,
      InputStream uploadedInputStream,
      FormDataContentDisposition fileDetail
  ) {
    final var file = documentService.findFileByTypeAndDocId(type, docId);
    logger.debug(" -> updating file: {}", file);

    final var version = fileService.createVersion(
        file,
        readContent(uploadedInputStream)
    );

    var filenameMetadata = new MetadataEntry("filename", fileDetail.getFileName());
    metadataService.update(file.getId(), filenameMetadata);

    logger.debug("Document [{}] has new version: {}", docId, version);
    return Response.ok().build();
  }
}
