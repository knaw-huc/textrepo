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
import javax.ws.rs.NotFoundException;
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
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create document")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultDocument.class, message = "OK")})
  public Response create(
      @Valid FormDocument form
  ) {
    logger.debug("create document: form={}", form);
    var doc = documentService.create(new Document(null, form.getExternalId()));
    return Response.ok(new ResultDocument(doc)).build();
  }

  @GET
  @Path("/{id}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Read document")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultDocument.class, message = "OK")})
  public Response get(
      @PathParam("id") @Valid UUID id
  ) {
    logger.debug("get document: id={}", id);
    final var doc = documentService
        .get(id)
        .orElseThrow(NotFoundException::new);
    return Response
        .ok(new ResultDocument(doc))
        .build();
  }

  @PUT
  @Path("/{id}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create or update document")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultDocument.class, message = "OK")})
  public Response update(
      @PathParam("id") @Valid UUID id,
      @Valid FormDocument form
  ) {
    logger.debug("update document: id={}; form={}", id, form);
    var doc = documentService.update(new Document(id, form.getExternalId()));
    return Response.ok(new ResultDocument(doc)).build();
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(value = "Delete document, its metadata and file links")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response delete(
      @PathParam("id") @Valid UUID id
  ) {
    logger.debug("delete document: id={}", id);
    documentService.delete(new Document(id, null));
    return Response.ok().build();
  }

}