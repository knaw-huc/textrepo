package nl.knaw.huc.resources.view;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.helpers.ContentsHelper;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringReader;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;

/*
 * This class is a Jersey sub-resource. It can be instantiated in other Jersey resources which
 * handle the first part of the URI which is responsible for fetching Contents and selecting
 * which 'view' is requested.
 * <p>
 * TODO: description
 *
 * @see nl.knaw.huc.resources.view.ViewBuilderFactory
 * @see nl.knaw.huc.resources.view.ViewVersionResource
 */
@Path("") // Without @Path("") this subresource is not resolved during tests
public class XmlViewerResource {
  private static final Logger log = LoggerFactory.getLogger(XmlViewerResource.class);

  private final Contents contents;
  private final ContentsHelper contentsHelper;

  public XmlViewerResource(Contents contents, ContentsHelper contentsHelper) {
    log.debug("XmlViewerResource created");
    this.contents = contents;
    this.contentsHelper = contentsHelper;
  }

  @GET
  @Path("xpath/{expr}")
  @Produces({APPLICATION_JSON, APPLICATION_XML})
  @Encoded
  public Response getXPath(
      @HeaderParam(ACCEPT_ENCODING) String acceptEncoding,
      @Encoded @PathParam("expr") @NotNull String expr
  ) {
    final var xpath = decode(expr, UTF_8);
    log.debug("getXPath: expr=[{}], decoded=[{}]", expr, xpath);

    final var xmlDoc = parse(contents);

    log.debug("parsed: [{}]", xmlDoc);

    final var nodes = xmlDoc.query(xpath);
    log.debug("nodes (size={}): {}", nodes.size(), nodes);
    nodes.forEach(n -> log.debug(n.getValue()));
    final var result = StreamSupport
        .stream(nodes.spliterator(), false)
        .map(Node::toXML)
        .peek(this::debugNode)
        .collect(toList());

    return Response.ok(result).build();
  }

  private void debugNode(String node) {
    if (log.isDebugEnabled()) {
      log.debug(format("node: [%s]", node));
    }
  }

  private static Document parse(Contents contents) {
    try {
      return new Builder().build(new StringReader(contents.asUtf8String()));
    } catch (ValidityException e) {
      throw new BadRequestException(format("document not valid XML: %s", e.getMessage()));
    } catch (ParsingException e) {
      throw new BadRequestException(format("document not well-formed: %s", e.getMessage()));
    } catch (IOException e) {
      throw new BadRequestException(format("failed to fully read document: %s", e.getMessage()));
    }
  }

  private Response asPlainTextAttachment(String text, String acceptEncoding) {
    return contentsHelper.asAttachment(text, acceptEncoding)
                         .header(CONTENT_TYPE, TEXT_PLAIN_TYPE)
                         .build();
  }
}
