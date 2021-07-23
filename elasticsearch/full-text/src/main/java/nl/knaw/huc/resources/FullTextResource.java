package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.api.MimetypeSubtypesResult;
import nl.knaw.huc.core.SupportedType;
import nl.knaw.huc.service.FieldsService;
import nl.knaw.huc.service.MappingService;
import nl.knaw.huc.service.SubtypeService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Path("/full-text")
public class FullTextResource {

  private final FieldsService fieldsService;
  private final MappingService mappingService;
  private final SubtypeService subtypeService;

  public FullTextResource(
      FieldsService fieldsService,
      MappingService mappingService,
      SubtypeService subtypeService
  ) {
    this.fieldsService = fieldsService;
    this.mappingService = mappingService;
    this.subtypeService = subtypeService;
  }

  @GET
  @Path("/types")
  @Timed
  @Produces(APPLICATION_JSON)
  public Response types() {
    return Response
        .status(200)
        .entity(SubtypeService.toTypesResultList(subtypeService.getMimetypeSubtypes()))
        .build();
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

    if (body.getMediaType() == null) {
      throw new IllegalArgumentException("Content-Type of file body part is missing");
    }
    var mimetype = subtypeService.determine(body.getMediaType().toString());
    var fields = fieldsService.createFields(
        inputStream,
        mimetype
    );

    return Response
        .status(200)
        .entity(fields).build();
  }

}
