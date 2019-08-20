package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.service.DocumentService;
import nl.knaw.huc.service.DocumentService.KeyValue;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.api.TextRepoFile.fromContent;
import static nl.knaw.huc.resources.ResourceUtils.readContent;

@Path("/documents")
public class DocumentsResource {
  private final Logger logger = LoggerFactory.getLogger(DocumentsResource.class);

  private final DocumentService documentService;

  public DocumentsResource(DocumentService documentService) {
    this.documentService = documentService;
  }

  @POST
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  public Response addDocument(@FormDataParam("file") InputStream uploadedInputStream,
                              @FormDataParam("file") FormDataContentDisposition fileDetail) {
    final var file = fromContent(readContent(uploadedInputStream));
    final var version = documentService.addDocument(file);
    return Response.created(locationOf(version)).build();
  }

  @POST
  @Timed
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @Path("/{uuid}/_meta")
  public Response addMetadata(@PathParam("uuid") @Valid UUID documentId, List<KeyValue> metadata) {
    logger.debug("addMetadata: uuid={}, metadata={}", documentId, metadata);
    documentService.addMetadata(documentId, metadata);
    return Response.ok().build();
  }

  @PUT
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @Path("/{uuid}/_file")
  public Response replaceDocumentFile(@PathParam("uuid") @Valid UUID documentId,
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
  @Path("/{uuid}/_meta")
  @Timed
  @Produces(APPLICATION_JSON)
  public Response getMetadata(@PathParam("uuid") @Valid UUID documentId) {
    return Response.ok().entity(documentService.getMetadata(documentId)).build();
  }

  @GET
  @Path("/{uuid}/_file")
  @Timed
  @Produces(APPLICATION_OCTET_STREAM)
  public Response getFile(@PathParam("uuid") @Valid UUID documentId) {
    var file = documentService.getLatestFile(documentId);
    return Response.ok(file.getContent(), APPLICATION_OCTET_STREAM)
                   .header("Content-Disposition", "attachment;")
                   .build();
  }

  @GET
  @Path("/{uuid}")
  @Timed
  @Produces(APPLICATION_JSON)
  public Response getLatestVersionOfDocument(@PathParam("uuid") @Valid UUID documentId) {
    logger.warn("getting latest version of: " + documentId.toString());
    var version = documentService.getLatestVersion(documentId);
    return Response.ok(version).build();
  }

  private static URI locationOf(Version version) {
    return UriBuilder.fromResource(DocumentsResource.class)
                     .path("{uuid}")
                     .build(version.getDocumentUuid());
  }

}
