package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.api.FormFile;
import nl.knaw.huc.api.MultipleLocations;
import nl.knaw.huc.api.ResultFile;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.service.DocumentService;
import nl.knaw.huc.service.ZipService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.resources.ResourceUtils.locationOf;
import static nl.knaw.huc.resources.ResourceUtils.readContent;
import static nl.knaw.huc.service.ZipService.isZip;

@Path("/documents")
public class DocumentsResource {
  private final Logger logger = LoggerFactory.getLogger(DocumentsResource.class);

  private final DocumentService documentService;
  private ZipService zipService;

  public DocumentsResource(
      DocumentService documentService,
      ZipService zipService
  ) {
    this.documentService = documentService;
    this.zipService = zipService;
  }

  @POST
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  public Response addDocument(
      @FormDataParam("file") InputStream inputStream,
      @FormDataParam("file") FormDataContentDisposition fileDetail,
      @FormDataParam("file") FormDataBodyPart bodyPart
  ) {
    if (isZip(bodyPart, fileDetail)) {
      var versions = zipService
        .handleZipFiles(inputStream)
        .stream()
        .map(this::handleNewDocument)
        .collect(toList());
      return Response.ok(new MultipleLocations(versions)).build();
    }

    var resultFile = handleNewDocument(new FormFile(
        fileDetail.getFileName(),
        readContent(inputStream)
    ));

    return Response.created(locationOf(resultFile.getVersion())).build();
  }

  private ResultFile handleNewDocument(FormFile formFile) {
    var version = documentService.createVersionWithFilenameMetadata(
        formFile.getContent(),
        formFile.getName()
    );
    return new ResultFile(formFile.getName(), version);
  }

  @GET
  @Path("/{uuid}")
  @Timed
  @Produces(APPLICATION_JSON)
  public Response getLatestVersionOfDocument(@PathParam("uuid") @Valid UUID documentId) {
    logger.info("getting latest version of: " + documentId.toString());
    var version = documentService.getLatestVersion(documentId);
    return Response.ok(version).build();
  }

}
