package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.ResultDocument;
import nl.knaw.huc.service.task.TaskBuilderFactory;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Api(tags = {"task", "register"})
@Path("task/register")
@Produces(APPLICATION_JSON)
public class RegisterIdentifiersResource {
  private static final Logger log = LoggerFactory.getLogger(RegisterIdentifiersResource.class);

  private static final String REGISTRATION_RESULT_MSG = "Returns list of (created / existing) documents";
  public static final String FILE_LAYOUT_NOTES = "File should contain a single externalId per line";

  private final TaskBuilderFactory factory;

  public RegisterIdentifiersResource(TaskBuilderFactory factory) {
    this.factory = requireNonNull(factory);
  }

  @POST
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Register documents by POSTing a file with externalIds in HTTP form, e.g., curl --form 'ids=@file'",
      notes = FILE_LAYOUT_NOTES)
  @ApiResponses(value = {
      @ApiResponse(
          code = 200,
          message = REGISTRATION_RESULT_MSG,
          response = ResultDocument.class, responseContainer = "List")})
  public List<ResultDocument> postFormIdentifiers(
      @NotNull
      @FormDataParam("ids")
          InputStream uploadedInputStream
  ) {
    return registerIdentifiers(uploadedInputStream);
  }

  @PUT
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Register documents by PUTing a file with externalIds, e.g., curl --form 'ids=@file'",
      notes = FILE_LAYOUT_NOTES)
  @ApiResponses(value = {
      @ApiResponse(
          code = 200,
          message = "Returns list of (created / existing) documents",
          response = ResultDocument.class, responseContainer = "List")})
  public List<ResultDocument> putIdentifiers(
      @NotNull
      @FormDataParam("ids")
          InputStream content
  ) {
    return registerIdentifiers(content);
  }

  private List<ResultDocument> registerIdentifiers(InputStream inputStream) {
    final var externalIds = new BufferedReader(new InputStreamReader(inputStream)).lines();
    final var registerIdentifiersTask = factory
        .getRegisterIdentifiersBuilder()
        .forExternalIdentifiers(externalIds)
        .build();

    final var docs = registerIdentifiersTask.run();
    log.debug("Registered {} identifiers", docs.size());
    return docs.stream()
               .map(ResultDocument::new)
               .collect(toList());
  }
}
