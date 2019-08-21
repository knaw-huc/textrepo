package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.service.DocumentService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.api.TextRepoFile.fromContent;
import static nl.knaw.huc.resources.ResourceUtils.readContent;

@Path("/documents/{uuid}/files")
public class DocumentFilesResource {
  private final Logger logger = LoggerFactory.getLogger(DocumentFilesResource.class);

  private final DocumentService documentService;

  public DocumentFilesResource(DocumentService documentService) {
    this.documentService = documentService;
  }

  @PUT
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  public Response replaceDocumentFile(
      @PathParam("uuid") @Valid UUID documentId,
      @FormDataParam("file") InputStream uploadedInputStream,
      @FormDataParam("file") FormDataContentDisposition fileDetail) {
    logger.debug("replacing file of document {}", documentId);
    final var file = fromContent(readContent(uploadedInputStream));
    final var now = LocalDateTime.now();
    final var version = documentService.replaceDocumentFile(documentId, file);

    if (version.getDate().isBefore(now)) {
      logger.debug("already current, not modified");
      return Response.notModified().build();  // this file is already the current version
    }

    return Response.ok(version).build();
  }

  @GET
  @Timed
  @Produces(APPLICATION_OCTET_STREAM)
  public Response getFile(@PathParam("uuid") @Valid UUID documentId) {
    var file = documentService.getLatestFile(documentId);
    return Response
        .ok(file.getContent(), APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment;")
        .build();
  }
}
