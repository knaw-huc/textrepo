package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.service.FieldsService;
import nl.knaw.huc.service.MappingService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.InputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.service.HeaderLinkUtil.extractId;

@Path("/file")
public class FileResource {

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

  @POST
  @Path("/fields")
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  public Response fields(
      @FormDataParam("file") InputStream inputStream,
      @NotNull @FormDataParam("file") FormDataBodyPart body
  ) {

    var mimetype = body.getMediaType().toString();
    if (mimetype == null) {
      throw new IllegalArgumentException("Content-Type of file body part is missing");
    }

    var link = body.getHeaders().get("Link").get(0);
    if (link == null) {
      throw new IllegalArgumentException("Origin Link of file body part is missing");
    }

    var fields = fieldsService.createFields(
        inputStream,
        mimetype,
        extractId(link)
    );

    return Response
        .status(200)
        .entity(fields).build();
  }

}
