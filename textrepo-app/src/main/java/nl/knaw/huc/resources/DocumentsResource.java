package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.db.FileDao;
import nl.knaw.huc.db.VersionDao;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Path("/documents")
public class DocumentsResource {
  private final Logger logger = LoggerFactory.getLogger(DocumentsResource.class);

  private final Jdbi jdbi;

  public DocumentsResource(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @POST
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  public Response addDocument(@FormDataParam("file") InputStream uploadedInputStream,
                              @FormDataParam("file") FormDataContentDisposition fileDetail) {
    final TextRepoFile file;
    try {
      file = TextRepoFile.fromContent(uploadedInputStream.readAllBytes());
    } catch (IOException e) {
      logger.warn("Could not read posted file, size={}", fileDetail.getSize());
      throw new BadRequestException("Could not read input stream of posted file", e);
    }

    getFileDao().insert(file);

    var version = new Version(UUID.randomUUID(), LocalDateTime.now(), file.getSha224());
    getVersionDao().insert(version);

    return Response.created(locationOf(version)).build();
  }

  @PUT
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @Path("/{uuid}")
  public Response replaceDocument(@PathParam("uuid") @Valid UUID documentId,
                                  @FormDataParam("file") InputStream uploadedInputStream,
                                  @FormDataParam("file") FormDataContentDisposition fileDetail) {
    logger.warn("storing new file for document {}", documentId);
    return Response.status(501).entity("not yet implemented").build();
  }

  @GET
  @Path("/{uuid}")
  @Timed
  @Produces(APPLICATION_JSON)
  public Response getLatestVersionOfDocument(@PathParam("uuid") @Valid UUID documentId) {
    logger.warn("getting latest version of: " + documentId.toString());
    var version = getLatestVersion(documentId);
    return Response.ok(version).build(); // TODO: yield file contents instead of Version object
  }

  private Version getLatestVersion(UUID uuid) {
    return getVersionDao()
      .findLatestByDocumentUuid(uuid)
      .orElseThrow(() -> new NotFoundException("No document for uuid: " + uuid));
  }

  private URI locationOf(Version version) {
    return UriBuilder.fromResource(DocumentsResource.class)
      .path("{uuid}")
      .build(version.getDocumentUuid());
  }

  private VersionDao getVersionDao() {
    return jdbi.onDemand(VersionDao.class);
  }

  private FileDao getFileDao() {
    return jdbi.onDemand(FileDao.class);
  }
}
