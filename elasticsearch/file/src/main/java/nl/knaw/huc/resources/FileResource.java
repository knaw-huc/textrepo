package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.service.FieldsService;
import nl.knaw.huc.service.MappingService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.service.HeaderLinkUtil.extractUuid;

@Path("/file")
public class FileResource {

  private static final Logger log = LoggerFactory.getLogger(FileResource.class);
  private final FieldsService fieldsService;
  private final MappingService mappingService;

  public FileResource(
      FieldsService fieldsService,
      MappingService mappingService
  ) {
    this.fieldsService = fieldsService;
    this.mappingService = mappingService;
  }

  @GET
  @Path("/mapping")
  @Timed
  @Produces(APPLICATION_JSON)
  public Response mapping() {
    return Response
        .status(200)
        .entity(mappingService.getMapping())
        .build();
  }

  /**
   * File indexer handles all mimetypes, since it only deals with metadata
   */
  @GET
  @Path("/types")
  @Timed
  @Produces(APPLICATION_JSON)
  public Response types() {
    return Response
        .status(Response.Status.NO_CONTENT)
        .build();
  }

  @POST
  @Path("/fields")
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  public Response fields(
      @NotNull @FormDataParam("file") FormDataBodyPart body
  ) {
    var mimetype = body.getMediaType().toString();
    if (mimetype == null) {
      throw new BadRequestException("Content-Type of file body part is missing");
    }

    var originLink = body.getHeaders().get("Link").get(0);
    if (originLink == null) {
      throw new BadRequestException("Origin Link of file body part is missing");
    }

    var fileId = extractUuid(originLink);

    log.info("Create fields for: " + fileId);
    var fields = fieldsService.createFields(fileId);
    log.info("Got fields");

    return Response
        .status(200)
        .entity(fields)
        .build();
  }

}
