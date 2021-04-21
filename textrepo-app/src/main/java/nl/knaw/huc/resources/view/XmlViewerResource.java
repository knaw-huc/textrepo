package nl.knaw.huc.resources.view;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.helpers.ContentsHelper;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.IllegalNameException;
import nu.xom.NamespaceConflictException;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.XPathContext;
import nu.xom.XPathException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
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
  @Path("xpath/{prefix}/{expr}")
  @Produces(APPLICATION_JSON)
  public Response getXPath(
      @HeaderParam(ACCEPT_ENCODING) String acceptEncoding,
      @PathParam("expr") @NotBlank @Encoded String expr,
      @PathParam("prefix") String defaultNamespacePrefix
  ) {
    final var xpath = decode(expr, UTF_8);
    log.debug("getXPath: expr=[{}], decoded=[{}]", expr, xpath);

    final var xmlDoc = parse(contents);
    log.debug("parsed: [{}]", xmlDoc);

    final var root = xmlDoc.getRootElement();
    log.debug("root element: [{}] has namespace prefix: [{}] and uri: [{}]",
        root.getLocalName(),
        root.getNamespacePrefix(),
        root.getNamespaceURI());

    final var declarationCount = root.getNamespaceDeclarationCount();
    log.debug("root element namespace declarationCount: {}", declarationCount);

    final XPathContext context = new XPathContext();
    for (int i = 0; i < declarationCount; i++) {
      final String prefix;
      final var curPrefix = root.getNamespacePrefix(i);
      final var nsUri = root.getNamespaceURI(curPrefix);
      log.debug(String.format(" %02d: prefix=%s, uri=%s%n", i, curPrefix, nsUri));

      if (StringUtils.isBlank(curPrefix)) {
        prefix = defaultNamespacePrefix;
      } else {
        prefix = curPrefix;
      }
      try {
        log.debug("registering namespace: [{}] -> [{}]", prefix, nsUri);
        context.addNamespace(prefix, nsUri);
      } catch (IllegalNameException | NamespaceConflictException e) {
        log.warn("failed to setup xpath context: {}", e.getMessage());
        throw new BadRequestException(e.getMessage());
      }
    }

    final Nodes nodes;
    try {
      nodes = xmlDoc.query(xpath, context);
    } catch (XPathException e) {
      log.warn("xpath failed: {}", e.getMessage());
      throw new BadRequestException(e.getMessage());
    }

    log.debug("xpath yielded {} node(s)", nodes.size());
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
      throw new BadRequestException(format("Document is not valid XML: %s", e.getMessage()));
    } catch (ParsingException e) {
      throw new BadRequestException(format("Document is not well-formed: %s", e.getMessage()));
    } catch (IOException e) {
      throw new BadRequestException(format("Failed to fully read document: %s", e.getMessage()));
    }
  }

  private Response asPlainTextAttachment(String text, String acceptEncoding) {
    return contentsHelper.asAttachment(text, acceptEncoding)
                         .header(CONTENT_TYPE, TEXT_PLAIN_TYPE)
                         .build();
  }
}
