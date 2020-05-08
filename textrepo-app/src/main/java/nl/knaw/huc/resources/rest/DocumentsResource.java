package nl.knaw.huc.resources.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.FormDocument;
import nl.knaw.huc.api.FormPageParams;
import nl.knaw.huc.api.ResultDocument;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.service.DocumentService;
import nl.knaw.huc.service.Paginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huc.service.Paginator.toResult;

@Api(tags = {"documents"})
@Path("/rest/documents")
public class DocumentsResource {

  private static final Logger log = LoggerFactory.getLogger(DocumentsResource.class);
  private final DocumentService documentService;
  private Paginator paginator;

  public DocumentsResource(
      DocumentService documentService,
      Paginator paginator
  ) {
    this.documentService = documentService;
    this.paginator = paginator;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create document")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultDocument.class, message = "OK")})
  public Response post(
      @Valid FormDocument form
  ) {
    log.debug("create document: {}", form);
    var doc = documentService.create(new Document(null, form.getExternalId()));
    log.debug("created document: {}", doc);
    return Response.ok(new ResultDocument(doc)).build();
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve documents, newest first")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultDocument.class, message = "OK")})
  public Response get(
      @QueryParam("externalId") String externalId,
      @QueryParam("createdAfter") LocalDateTime createdAfter,
      @BeanParam FormPageParams pageParams
  ) {
    log.debug("get documents: externalId={}; createdAfter={}; pageParams={}", externalId, createdAfter, pageParams);
    var docs = documentService.getAll(externalId, createdAfter, paginator.fromForm(pageParams));
    log.debug("got documents: {}", docs);
    return Response
        .ok(toResult(docs, ResultDocument::new))
        .build();
  }

  @GET
  @Path("/{id}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve document")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultDocument.class, message = "OK")})
  public Response get(
      @PathParam("id") @Valid UUID id
  ) {
    log.debug("get document: id={}", id);
    final var doc = documentService
        .get(id)
        .orElseThrow(NotFoundException::new);
    log.debug("got document: {}", doc);
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
  public Response put(
      @PathParam("id") @Valid UUID id,
      @Valid FormDocument form
  ) {
    log.debug("update document: id={}; form={}", id, form);
    var doc = documentService.update(new Document(id, form.getExternalId()));
    log.debug("updated document: {}", doc);
    return Response.ok(new ResultDocument(doc)).build();
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(value = "Delete document")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response delete(
      @PathParam("id") @Valid UUID id
  ) {
    log.debug("delete document: id={}", id);
    documentService.delete(id);
    log.debug("deleted document");
    return Response.ok().build();
  }

}
