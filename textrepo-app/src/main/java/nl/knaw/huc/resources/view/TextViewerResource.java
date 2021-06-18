package nl.knaw.huc.resources.view;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.helpers.ContentsHelper;
import nl.knaw.huc.resources.view.text.TextCharsResolver;
import nl.knaw.huc.resources.view.text.TextLinesResolver;
import nl.knaw.huc.resources.view.text.TextRangeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;

/*
 * This class is a Jersey sub-resource. It can be instantiated in other Jersey resources which
 * handle the first part of the URI which is responsible for fetching Contents and selecting
 * which 'view' is requested.
 * <p>
 * So in TextViewerResource we can abstract from whatever it was that yielded the Contents,
 * and what mechanism determined that 'we' should be the object handling the view details.
 * <p>
 * Then, TextViewerResource will take care of the rest of the URI, addressing the following:
 * - …/chars/{startOffset}/{endOffset}
 * - …/lines/{startLine}/{endLine}
 * - …range/{startLine}/startCharOffset}/{endLine}/{endCharOffset}
 *
 * @see nl.knaw.huc.resources.view.ViewBuilderFactory
 * @see nl.knaw.huc.resources.view.ViewVersionResource
 */
@Path("") // Without @Path("") this subresource is not resolved during tests
public class TextViewerResource {
  private static final Logger log = LoggerFactory.getLogger(TextViewerResource.class);

  private final Contents contents;
  private final ContentsHelper contentsHelper;

  public TextViewerResource(Contents contents, ContentsHelper contentsHelper) {
    this.contents = contents;
    this.contentsHelper = contentsHelper;
  }

  @GET
  @Path("chars/{startOffset}/{endOffset}")
  @Produces(TEXT_PLAIN)
  public Response getChars(
      @HeaderParam(ACCEPT_ENCODING) String acceptEncoding,
      @PathParam("startOffset") @NotNull RangeParam startParam,
      @PathParam("endOffset") @NotNull RangeParam endParam
  ) {
    log.debug("getChars: startParam=[{}], endParam=[{}]", startParam, endParam);

    final var resolver = new TextCharsResolver(startParam, endParam);
    final var result = resolver.resolve(contents);

    return asPlainTextAttachment(result, acceptEncoding);
  }

  @GET
  @Path("lines/{startOffset}/{endOffset}")
  @Produces(TEXT_PLAIN)
  public Response getLines(
      @HeaderParam(ACCEPT_ENCODING) String acceptEncoding,
      @PathParam("startOffset") @NotNull RangeParam startParam,
      @PathParam("endOffset") @NotNull RangeParam endParam
  ) {
    log.debug("getLines: startParam=[{}], endParam=[{}]", startParam, endParam);

    final var resolver = new TextLinesResolver(startParam, endParam);
    final var result = resolver.resolve(contents);

    return asPlainTextAttachment(result, acceptEncoding);
  }

  @GET
  @Path("range/{startLineOffset}/{startCharOffset}/{endLineOffset}/{endCharOffset}")
  // E.g., from line 3, char 12 (on that line) up to and including line 8, char 4 (on that line)
  @Produces(TEXT_PLAIN)
  public Response getRange(
      @HeaderParam(ACCEPT_ENCODING) String acceptEncoding,
      @PathParam("startLineOffset") @NotNull RangeParam startLineParam,
      @PathParam("startCharOffset") @NotNull RangeParam startCharParam,
      @PathParam("endLineOffset") @NotNull RangeParam endLineParam,
      @PathParam("endCharOffset") @NotNull RangeParam endCharParam
  ) {
    log.debug("getLines: startLineParam=[{}], startCharParam=[{}], endLineParam=[{}], endCharParam=[{}]",
        startLineParam, startCharParam, endLineParam, endCharParam);

    final var resolver = new TextRangeResolver(startLineParam, startCharParam, endLineParam, endCharParam);
    final var result = resolver.resolve(contents);

    return asPlainTextAttachment(result, acceptEncoding);
  }

  private Response asPlainTextAttachment(String text, String acceptEncoding) {
    return contentsHelper.asAttachment(text, acceptEncoding)
                         .header(CONTENT_TYPE, TEXT_PLAIN_TYPE)
                         .build();
  }
}
