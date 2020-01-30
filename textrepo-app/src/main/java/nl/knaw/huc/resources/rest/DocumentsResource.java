package nl.knaw.huc.resources.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.FormDocument;
import nl.knaw.huc.api.ResultDocument;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
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

@Api(tags = {"documents"})
@Path("/rest/documents")
public class DocumentsResource {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final DocumentService documentService;

  public DocumentsResource(
      DocumentService documentService
  ) {
    this.documentService = documentService;
  }

  @POST
  @Path("/{id}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create document")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultDocument.class, message = "OK")})
  public Response createDocument(
      @Valid FormDocument form
  ) {
    logger.debug("createDocument: form={}", form);
    var doc = documentService.create(new Document(null, form.getExternalId()));
    return Response.ok(new ResultDocument(doc)).build();
  }

  @GET
  @Path("/{id}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Read document")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultDocument.class, message = "OK")})
  public Response getDocument(
      @PathParam("id") @Valid UUID id
  ) {
    logger.debug("getDocument: id={}", id);
    final var doc = documentService.get(id);
    return Response.ok(new ResultDocument(doc)).build();
  }

  @PUT
  @Path("/{id}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Update document")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultDocument.class, message = "OK")})
  public Response updateDocument(
      @PathParam("id") @Valid UUID id,
      @Valid FormDocument form
  ) {
    logger.debug("updateDocument: id={}; form={}", id, form);
    var doc = documentService.update(new Document(id, form.getExternalId()));
    return Response.ok(new ResultDocument(doc)).build();
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(value = "Delete document, its metadata and file links")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response deleteDocument(
      @PathParam("id") @Valid UUID id
  ) {
    logger.debug("deleteDocument: id={}", id);
    documentService.delete(new Document(id, null));
    return Response.ok().build();
  }

}
