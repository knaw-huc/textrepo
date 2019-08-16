package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.service.DocumentService;
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
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
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
    var version = documentService.addDocument(readContent(uploadedInputStream));
    logger.debug("added new document {}", version.getDocumentUuid());
    return Response.created(locationOf(version)).build();
  }

  @PUT
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @Path("/{uuid}/_file")
  public Response replaceDocument(@PathParam("uuid") @Valid UUID documentId,
                                  @FormDataParam("file") InputStream uploadedInputStream,
                                  @FormDataParam("file") FormDataContentDisposition fileDetail) {
    logger.warn("replacing file of document {}", documentId);
    LocalDateTime now = LocalDateTime.now();
    var version = documentService.replaceDocument(documentId, readContent(uploadedInputStream));

    if (version.getDate().isBefore(now)) {
      return Response.notModified().build();
    }

    return Response.ok()
            .entity(version)
            .build();
  }

  @GET
  @Path("/{uuid}")
  @Timed
  @Produces(APPLICATION_JSON)
  public Response getLatestVersionOfDocument(@PathParam("uuid") @Valid UUID documentId) {
    logger.warn("getting latest version of: " + documentId.toString());
    var version = documentService.getLatestVersion(documentId);
    return Response.ok(version).build(); // TODO: yield file contents instead of Version object
  }

  private static URI locationOf(Version version) {
    return UriBuilder.fromResource(DocumentsResource.class)
            .path("{uuid}")
            .build(version.getDocumentUuid());
  }

}
