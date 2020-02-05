package nl.knaw.huc.resources.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.FormTextrepoFile;
import nl.knaw.huc.api.ResultTextrepoFile;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(tags = {"files"})
@Path("/rest/files")
public class FilesResource {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final FileService fileService;

  public FilesResource(FileService fileService) {
    this.fileService = fileService;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create file")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultTextrepoFile.class, message = "OK")})
  public Response post(
      @Valid FormTextrepoFile form
  ) {
    logger.debug("create file: form={}", form);
    var file = fileService.create(form.getDocId(), new TextrepoFile(null, form.getTypeId()));
    return Response.ok(new ResultTextrepoFile(form.getDocId(), file)).build();
  }

  @GET
  @Path("/{id}")
  @Timed
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve file")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultTextrepoFile.class, message = "OK")})
  public Response get(
      @PathParam("id") @NotNull @Valid UUID id
  ) {
    logger.debug("get file: id={}", id);
    var file = fileService.get(id);
    return Response.ok(new ResultTextrepoFile(fileService.getDocumentId(file.getId()), file)).build();
  }

  @PUT
  @Path("/{id}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create or update file")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultTextrepoFile.class, message = "OK")})
  public Response put(
      @PathParam("id") @Valid UUID id,
      @Valid FormTextrepoFile form
  ) {
    logger.debug("upsert file: id={}; form={}", id, form);
    var file = fileService.upsert(form.getDocId(), new TextrepoFile(id, form.getTypeId()));
    return Response.ok(new ResultTextrepoFile(form.getDocId(), file)).build();
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(value = "Delete file")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response delete(
      @PathParam("id") @Valid UUID id
  ) {
    logger.debug("delete file: id={}", id);
    fileService.delete(id);
    return Response.ok().build();
  }

}
