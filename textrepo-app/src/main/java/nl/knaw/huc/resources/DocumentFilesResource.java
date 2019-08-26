package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.api.MultipleLocations;
import nl.knaw.huc.api.ResultFile;
import nl.knaw.huc.service.DocumentFileService;
import nl.knaw.huc.service.ZipService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
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
import java.util.Objects;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.api.TextRepoFile.fromContent;
import static nl.knaw.huc.resources.ResourceUtils.readContent;
import static nl.knaw.huc.service.ZipService.isZip;

@Path("/documents/{uuid}/files")
public class DocumentFilesResource {
  private final Logger logger = LoggerFactory.getLogger(DocumentFilesResource.class);

  private final DocumentFileService documentFileService;
  private ZipService zipService;

  public DocumentFilesResource(DocumentFileService documentFileService, ZipService zipService) {
    this.documentFileService = documentFileService;
    this.zipService = zipService;
  }

  // TODO: update filename metadata
  @PUT
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  public Response updateDocumentFile(
      @PathParam("uuid") @Valid UUID documentId,
      @FormDataParam("file") InputStream uploadedInputStream,
      @FormDataParam("file") FormDataContentDisposition fileDetail,
      @FormDataParam("file") FormDataBodyPart bodyPart
  ) {
    if (isZip(bodyPart, fileDetail)) {
      var versions = zipService
        .handleZipFiles(uploadedInputStream)
        .stream()
        .map(file -> handleUpdate(documentId, file.getContent(), file.getName()))
        .filter(Objects::nonNull)
        .collect(toList());
      return Response.ok(new MultipleLocations(versions)).build();
    }

    var version = handleUpdate(
      documentId,
      readContent(uploadedInputStream),
      fileDetail.getFileName()
    );
    return Response.ok(version).build();
  }

  /**
   * Try to update
   *
   * @return new version, or null if not replaced
   */
  private ResultFile handleUpdate(
      UUID documentId,
      byte[] content,
      String filename
  ) {
    logger.debug("replacing file of document [{}]", documentId);
    var file = fromContent(content);
    var version = documentFileService.replaceDocumentFile(documentId, file, filename);

    if (version.getDate().isBefore(now())) {
      logger.info("skip existing [{}]", filename);
      return null;
    }

    return new ResultFile(filename, version);
  }

  @GET
  @Timed
  @Produces(APPLICATION_OCTET_STREAM)
  public Response getFile(@PathParam("uuid") @Valid UUID documentId) {
    var file = documentFileService.getLatestFile(documentId);
    return Response
        .ok(file.getContent(), APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment;")
        .build();
  }
}
