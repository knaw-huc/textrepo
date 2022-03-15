package nl.knaw.huc.resources.rest;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huc.resources.HeaderLink.Uri.FILE;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import nl.knaw.huc.api.FormTextRepoFile;
import nl.knaw.huc.api.ResultTextRepoFile;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.service.file.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = {"files"})
@Path("/rest/files")
public class FilesResource {

  private static final Logger log = LoggerFactory.getLogger(FilesResource.class);

  private final FileService fileService;

  public FilesResource(FileService fileService) {
    this.fileService = requireNonNull(fileService);
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create file, adds file to indices")
  @ApiResponses(value = {
      @ApiResponse(code = 201, response = ResultTextRepoFile.class, message = "Created")})
  public Response createFile(
      @Valid FormTextRepoFile form
  ) {
    log.debug("Create file: form={}", form);
    var file = fileService.insert(form.docId, new TextRepoFile(null, form.typeId));
    log.debug("Created file: {}", file);
    return Response
        .created(FILE.build(file.getId()))
        .entity(new ResultTextRepoFile(form.docId, file))
        .build();
  }

  @GET
  @Path("/{id}")
  @Timed
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve file")
  @ApiResponses(value = {
      @ApiResponse(code = 200, response = ResultTextRepoFile.class, message = "OK")})
  public Response getFile(
      @PathParam("id")
      @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
      @NotNull
      @Valid
      UUID id
  ) {
    log.debug("Get file: id={}", id);
    var file = fileService.get(id);
    var docId = fileService.getDocumentId(file.getId());
    log.debug("Got file: {}", file);
    return Response.ok(new ResultTextRepoFile(docId, file)).build();
  }

  @PUT
  @Path("/{id}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create or update file, add or update file in indices")
  @ApiResponses(value = {
      @ApiResponse(code = 200, response = ResultTextRepoFile.class, message = "OK")})
  public Response putFile(
      @PathParam("id")
      @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
      @Valid
      UUID id,
      @Valid
      FormTextRepoFile form
  ) {
    log.debug("Create or update file: id={}; form={}", id, form);
    var file = fileService.upsert(form.docId, new TextRepoFile(id, form.typeId));
    log.debug("Created or updated file: {}", file);
    return Response.ok(new ResultTextRepoFile(form.docId, file)).build();
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(value = "Delete file")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "No Content")})
  public Response deleteFile(
      @PathParam("id")
      @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
      @Valid
      UUID id
  ) {
    log.debug("Delete file: id={}", id);
    fileService.delete(id);
    log.debug("Deleted file");
    return Response.noContent().build();
  }

}
