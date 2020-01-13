package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.api.Fields;
import nl.knaw.huc.core.SupportedType;
import nl.knaw.huc.service.FieldsService;
import nl.knaw.huc.service.MappingService;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.InputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

// TODO: create mapping for keyword search
// TODO: return mapping
// TODO: add health check
// TODO: test in combination with textrepo
// TODO: create concordion test
@Path("/autocomplete")
public class AutocompleteResource {

  private final FieldsService fieldsService;
  private final MappingService mappingService;

  public AutocompleteResource(
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
      @QueryParam("mimetype") String mimetype,
      @FormDataParam("file") InputStream inputStream
  ) {

    var fields = fieldsService.createFieldsForType(
        inputStream,
        SupportedType.fromString(mimetype)
    );

    return Response
        .status(200)
        .entity(fields).build();
  }

}
